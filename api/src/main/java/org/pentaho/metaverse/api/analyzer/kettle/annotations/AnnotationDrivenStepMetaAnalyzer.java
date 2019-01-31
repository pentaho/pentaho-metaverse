/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.api.analyzer.kettle.annotations;

import com.google.common.base.Strings;
import com.tinkerpop.blueprints.Direction;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaNone;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.dictionary.DictionaryHelper;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer.RESOURCE;

/**
 * KettleGenericStepMetaAnalyzer provides a default implementation for analyzing PDI step
 * to gather metadata for the metaverse.
 */
public class AnnotationDrivenStepMetaAnalyzer extends StepAnalyzer<BaseStepMeta> {
  private final transient Class<? extends BaseStepMeta> stepClass;
  private final AtomicBoolean streamFieldsLoaded = new AtomicBoolean( false );

  private final Map<String, String> typeCategoryMap;
  private final transient EntityRegister register;

  @SuppressWarnings ( "unused" )
  public AnnotationDrivenStepMetaAnalyzer( BaseStepMeta meta ) {
    this( meta, DictionaryHelper.typeCategoryMap, DictionaryHelper::registerEntityType );
  }

  AnnotationDrivenStepMetaAnalyzer( BaseStepMeta meta, Map<String, String> typeCategoryMap, EntityRegister register ) {
    stepClass = meta.getClass();
    this.typeCategoryMap = typeCategoryMap;
    this.register = register;
    registerTypes();
  }

  @Override
  protected void customAnalyze( BaseStepMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    loadStreamFields( meta );

    // handles any @Metaverse.Node annotations in the meta, generating a map of nodeName->node
    Map<String, IMetaverseNode> externalNodes = getFieldStream( meta, Metaverse.Node.class )
      .map( field -> attachNodes( meta, rootNode, field ) )
      .collect( Collectors.toMap( Pair::left, Pair::right ) );

    // attach resource fields
    getFieldStream( meta, Metaverse.NodeLink.class )
      .map( field -> field.getAnnotation( Metaverse.NodeLink.class ) )
      .filter( attribute -> !attribute.parentNodelink().isEmpty() )
      .forEach( attribute -> linkResourceFieldToNode( externalNodes, attribute ) );

    // set stepnode properties
    rootNode.setProperties(
      getFieldStream( meta, Metaverse.Property.class )
        .collect( Collectors.toMap( this::getPropertyName, field -> getMetaFieldValue( meta, field ) ) ) );
  }

  private Stream<Field> getFieldStream( BaseStepMeta meta, Class<? extends Annotation> annotation ) {
    return stream( meta.getClass().getFields() )
      .filter( field -> field.isAnnotationPresent( annotation ) );
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return Collections.singleton( stepClass );
  }

  @Override
  protected Set<StepField> getUsedFields( BaseStepMeta meta ) {
    loadStreamFields( meta );
    return getFieldStream( meta, Metaverse.Property.class )
      .map( field -> getMetaFieldValue( meta, field ) )
      .map( this::stepNameFieldName )
      .filter( Optional::isPresent )
      .map( Optional::get )
      .map( stepField -> new StepField( stepField.left, stepField.right ) )
      .collect( toSet() );
  }

  /**
   * Adds any linked resource nodes to the output row meta.
   * Applicable to something like a Message field being sent to an external queue.
   */
  @Override protected Map<String, RowMetaInterface> getOutputRowMetaInterfaces( BaseStepMeta meta ) {
    Map<String, RowMetaInterface> rowMetas = super.getOutputRowMetaInterfaces( meta );

    RowMeta resoureRowMeta = new RowMeta();
    stream( meta.getClass().getFields() )
      .filter( this::isLinked )
      .forEach( field -> resoureRowMeta.addValueMeta( new ValueMetaNone( getPropertyName( field ) ) ) );

    if ( resoureRowMeta.size() > 0 ) {
      rowMetas.put( RESOURCE, resoureRowMeta );
    }
    return rowMetas;
  }

  /**
   * output rowmeta limited to the "used" fields
   */
  @Override public RowMetaInterface getOutputFields( BaseStepMeta meta ) {
    RowMetaInterface rowMeta = super.getOutputFields( meta );
    RowMetaInterface usedRowMeta = new RowMeta();
    getUsedFields( meta ).stream()
      .map( StepField::getFieldName )
      .map( rowMeta::indexOfValue )
      .filter( index -> index >= 0 )
      .forEach( index -> usedRowMeta.addValueMeta( rowMeta.getValueMeta( index ) ) );

    return usedRowMeta;
  }

  /**
   * Registers Entity->Category and Entity->Entity mappings with {@link DictionaryHelper}
   */
  private void registerTypes() {
    stream( stepClass.getAnnotationsByType( Metaverse.CategoryMap.class ) )
      .forEach( catMap -> typeCategoryMap.put( catMap.entity(), catMap.category() ) );

    stream( stepClass.getAnnotationsByType( Metaverse.EntityLink.class ) )
      .forEach( entityLink -> {
        String parentEntity = entityLink.parentEntity().isEmpty() ? null : entityLink.parentEntity();
        register.registerEntityTypes( entityLink.link(), entityLink.entity(), parentEntity );
      } );

  }

  private void linkResourceFieldToNode( Map<String, IMetaverseNode> resourceNodes, Metaverse.NodeLink attribute ) {
    StepNodes stepNodes = isOutLink( attribute ) ? getOutputs() : getInputs();
    if ( stepNodes == null ) {
      return;  // nothing to link
    }
    IMetaverseNode resourceFieldNode = stepNodes.findNode( RESOURCE, attribute.nodeName() );
    resourceNodes.entrySet().stream()
      .filter( entry -> entry.getKey().equals( attribute.parentNodeName() ) )
      .findFirst()
      .ifPresent( entry -> addLink( attribute, resourceFieldNode, entry.getValue() ) );

  }

  /**
   * Adds link with correct direction based on IN or OUT
   */
  private void addLink( Metaverse.NodeLink attribute, IMetaverseNode resourceFieldNode,
                                     IMetaverseNode resourceNode ) {
    if ( isOutLink( attribute ) ) {
      getMetaverseBuilder().addLink( resourceNode, attribute.parentNodelink(), resourceFieldNode );
    } else {
      getMetaverseBuilder().addLink( resourceFieldNode, attribute.parentNodelink(), resourceNode );
    }
  }


  private boolean isLinked( Field field ) {
    return field.isAnnotationPresent( Metaverse.NodeLink.class );
  }

  private boolean isOutLink( Metaverse.NodeLink attribute ) {
    return attribute.linkDirection().equals( Direction.OUT.name() );
  }

  /**
   * loads the prevFields and stepFields collections in {@link StepAnalyzer}
   */
  private void loadStreamFields( BaseStepMeta meta ) {
    if ( !streamFieldsLoaded.getAndSet( true ) ) {
      loadInputAndOutputStreamFields( meta );
    }
  }

  private Optional<Pair<String, String>> stepNameFieldName( String fieldName ) {
    List<Map.Entry<String, RowMetaInterface>> fieldMatches =
      prevFields.entrySet().stream()
        .filter( tuple -> asList( tuple.getValue().getFieldNames() ).contains( fieldName ) )
        .collect( Collectors.toList() );

    if ( fieldMatches.size() != 1 ) {
      return Optional.empty();
    }
    return Optional.of( new Pair<>( fieldMatches.get( 0 ).getKey(), fieldName ) );
  }

  private Pair<String, IMetaverseNode> attachNodes( BaseStepMeta meta, IMetaverseNode rootNode, Field resource ) {
    Metaverse.Node nodeAnnotation = resource.getAnnotation( Metaverse.Node.class );
    IMetaverseNode node = createNode( rootNode, nodeAnnotation, resource, meta );

    // set the node properties from annotated meta
    node.setProperties(
      stream( meta.getClass().getFields() )
        .filter( field -> fieldAnnotatedSameType( field, nodeAnnotation.name() ) )
        .collect( Collectors.toMap( this::getPropertyName, field -> getMetaFieldValue( meta, field ) ) ) );

    // link the node to the graph
    getMetaverseBuilder()
      .addNode( node )
      .addLink( node, nodeAnnotation.link(), rootNode );

    return new Pair<>( nodeAnnotation.name(), node );
  }

  private IMetaverseNode createNode( IMetaverseNode rootNode, Metaverse.Node annotation,
                                     Field externalResource, BaseStepMeta meta ) {
    MetaverseComponentDescriptor componentDescriptor = new MetaverseComponentDescriptor(
      getNodeName( externalResource, meta ), annotation.type(),
      rootNode, getDescriptor().getContext() );
    return createNodeFromDescriptor( componentDescriptor );
  }

  /**
   * Name is the value of the field in the meta.
   * If there is no assigned value to the field, uses the property name.
   */
  private String getNodeName( Field nodeField, BaseStepMeta meta ) {
    String metaFieldValue = getMetaFieldValue( meta, nodeField );
    if ( Strings.isNullOrEmpty( metaFieldValue ) ) {
      metaFieldValue = getPropertyName( nodeField );
    }
    return metaFieldValue;
  }

  private boolean fieldAnnotatedSameType( Field field, String externalResourceName ) {
    return field.isAnnotationPresent( Metaverse.Property.class )
      && field.getAnnotation( Metaverse.Property.class ).parentNodeName().equals( externalResourceName );
  }

  private String getMetaFieldValue( BaseStepMeta meta, Field field ) {
    try {
      String value = field.get( meta ).toString();
      return meta.getParentStepMeta().getParentTransMeta().environmentSubstitute( value );
    } catch ( IllegalAccessException e ) {
      throw new IllegalStateException( e );
    }
  }

  private String getPropertyName( Field field ) {
    String annotatedName = field.getAnnotation( Metaverse.Property.class ).name();
    if ( Strings.isNullOrEmpty( annotatedName ) ) {
      annotatedName = field.getName();
    }
    return annotatedName.toLowerCase();
  }

  /**
   * Used to reduce coupling with the static DictionaryHelper
   */
  @FunctionalInterface
  interface EntityRegister {
    void registerEntityTypes( String linkType, String entityType, String parentEntityType );
  }

  private static class Pair<L, R> {
    final L left;
    final R right;

    Pair( L l, R r ) {
      left = l;
      right = r;
    }

    public L left() {
      return left;
    }

    public R right() {
      return right;
    }
  }

}

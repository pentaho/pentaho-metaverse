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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer.RESOURCE;

/**
 * Allows metaverse structure to be defined via annotations on the StepMetaInterface impl.
 * Can be extended for relationships that can't be captured via these annotations.
 * See README.md
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

  /**
   * Graph updates are mostly driven by this method.  It handles
   *
   * 1)  Creating nodes specified with @Metaverse.Node
   * 2)  Linking any nodes marked with @Metaverse.LinkNode
   * 3)  Updating all node properties (@Metaverse.Property).
   */
  @Override
  protected void customAnalyze( BaseStepMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    loadStreamFields( meta );
    AnnotatedClassFields annoFields = new AnnotatedClassFields( meta );

    // handles any @Metaverse.Node annotations in the meta, generating a map of nodeName->node
    Map<AnnotatedClassField<Metaverse.Node>, IMetaverseNode> externalNodes = annoFields.nodes()
      .map( field -> attachNodes( rootNode, field, annoFields ) )
      .collect( toMap( Pair::left, Pair::right ) );

    // attach resource fields
    annoFields.links()
      .forEach( nodeLink -> linkResourceFieldToNode( externalNodes, nodeLink.annotation, annoFields ) );

    // set stepnode properties
    rootNode.setProperties(
      annoFields.props()
        .collect( toMap( AnnotatedClassField::name, AnnotatedClassField::val ) ) );
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return Collections.singleton( stepClass );
  }

  @Override
  protected Set<StepField> getUsedFields( BaseStepMeta meta ) {
    loadStreamFields( meta );
    return new AnnotatedClassFields( meta ).props()
      .map( AnnotatedClassField::val )
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

    RowMeta resourceRowMeta = new RowMeta();
    new AnnotatedClassFields( meta )
      .links()
      .forEach( field -> resourceRowMeta.addValueMeta( new ValueMetaNone( field.name ) ) );

    if ( resourceRowMeta.size() > 0 ) {
      rowMetas.put( RESOURCE, resourceRowMeta );
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

  private void linkResourceFieldToNode( Map<AnnotatedClassField<Metaverse.Node>, IMetaverseNode> resourceNodes,
                                        Metaverse.NodeLink nodeLink,
                                        AnnotatedClassFields annoFields ) {
    StepNodes stepNodes = isOutLink( nodeLink ) ? getOutputs() : getInputs();
    if ( stepNodes == null ) {
      return;  // nothing to link
    }
    IMetaverseNode resourceFieldNode = stepNodes.findNode( RESOURCE, nodeLink.nodeName() );

    annoFields.node( nodeLink.parentNodeName() )
      .map( resourceNodes::get )
      .ifPresent( parentNode -> addLink( nodeLink, resourceFieldNode, parentNode ) );
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

  private Pair<AnnotatedClassField<Metaverse.Node>, IMetaverseNode> attachNodes( IMetaverseNode rootNode,
                                                                                 AnnotatedClassField<Metaverse.Node> resource,
                                                                                 AnnotatedClassFields annoFields ) {
    Metaverse.Node nodeAnno = resource.annotation;
    IMetaverseNode node = createNode( rootNode, resource );

    // set the node properties from annotated meta
    node.setProperties(
      annoFields.props()
        .filter( field -> field.annotation.parentNodeName().equals( nodeAnno.name() ) )
        .collect( toMap( AnnotatedClassField::name, AnnotatedClassField::val ) ) );

    // link the node to the graph
    if ( nodeAnno.linkDirection().equals( Direction.OUT.name() ) ) {
      getMetaverseBuilder()
        .addNode( node )
        .addLink( node, nodeAnno.link(), rootNode );
    } else {
      getMetaverseBuilder()
        .addNode( node )
        .addLink( rootNode, nodeAnno.link(), node );
    }

    return new Pair<>( resource, node );
  }

  private IMetaverseNode createNode( IMetaverseNode rootNode, AnnotatedClassField<Metaverse.Node> field ) {
    return createNodeFromDescriptor(
      new MetaverseComponentDescriptor(
        field.name, field.annotation.type(), rootNode, getDescriptor().getContext() ) );
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

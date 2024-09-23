/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import com.tinkerpop.blueprints.Direction;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaNone;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.ISubTransAwareMeta;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.dictionary.DictionaryHelper;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.pentaho.metaverse.api.analyzer.kettle.step.SubtransAnalyzer;
import org.pentaho.metaverse.api.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.pentaho.dictionary.DictionaryConst.LINK_OUTPUTS;
import static org.pentaho.dictionary.DictionaryConst.PROPERTY_TYPE;
import static org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer.RESOURCE;

/**
 * Allows metaverse structure to be defined via annotations on the StepMetaInterface impl.
 * Can be extended for relationships that can't be captured via these annotations.
 * See README.md
 */
public class AnnotationDrivenStepMetaAnalyzer extends StepAnalyzer<BaseStepMeta> {
  private final transient Logger log = LoggerFactory.getLogger( AnnotationDrivenStepMetaAnalyzer.class );

  private final transient Class<? extends BaseStepMeta> stepClass;

  private final Map<String, String> typeCategoryMap;
  private final transient EntityRegister register;
  private final transient VariableSpace space;
  private final transient BaseStepMeta meta;

  public AnnotationDrivenStepMetaAnalyzer( BaseStepMeta meta ) {
    this( meta, DictionaryHelper.typeCategoryMap, DictionaryHelper::registerEntityType, meta.getParentStepMeta().getParentTransMeta() );
  }

  public AnnotationDrivenStepMetaAnalyzer( BaseStepMeta meta, VariableSpace space ) {
    this( meta, DictionaryHelper.typeCategoryMap, DictionaryHelper::registerEntityType, space );
  }

  AnnotationDrivenStepMetaAnalyzer( BaseStepMeta meta, Map<String, String> typeCategoryMap, EntityRegister register, VariableSpace space ) {
    this.meta = meta;
    this.stepClass = meta.getClass();
    this.typeCategoryMap = typeCategoryMap;
    this.register = register;
    this.space = space;
    registerTypes();
  }

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new AnnotationDrivenStepMetaAnalyzer( this.meta );
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
    AnnotatedClassFields annoFields = new AnnotatedClassFields( meta, space );

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

    // check for a subtrans and generate the graph
    if ( meta instanceof StepWithMappingMeta ) {
      analyzeAndLinkSubtrans( meta, rootNode, externalNodes );
    }
  }

  private void analyzeAndLinkSubtrans( Object meta, IMetaverseNode rootNode,
                                       Map<AnnotatedClassField<Metaverse.Node>, IMetaverseNode> externalNodes )
    throws MetaverseAnalyzerException {
    try {
      // analyze subtrans and generate lineage graph
      TransMeta subTransMeta = TransExecutorMeta
        .loadMappingMeta( parentTransMeta.getBowl(), (StepWithMappingMeta) meta, parentTransMeta.getRepository(),
          parentTransMeta.getMetaStore(), parentTransMeta );

      IMetaverseNode subTransNode =
        KettleAnalyzerUtil.analyze( this, parentTransMeta, (ISubTransAwareMeta) meta, rootNode );

      SubtransAnalyzer<BaseStepMeta> subtransAnalyzer = getSubtransAnalyzer();

      linkToSubTransInputs( externalNodes, subTransMeta, subTransNode, subtransAnalyzer );

      String resultFieldsStep = getSubTransResultFieldName();

      if ( null != resultFieldsStep && !resultFieldsStep.equals( "" ) ) {
        linkToSubTransOutputs( subTransMeta, subTransNode, subtransAnalyzer, resultFieldsStep );
      }  else {
        log.warn( Messages.getString( "ERROR.AnnotationDrivenStepMetaAnalyzer.SubtransResultNotFound" ) );
      }

    } catch ( KettleException e ) {
      log.error(
        Messages.getErrorString( "WARN.AnnotationDrivenStepMetaAnalyzer.SubtransProcessingException" ), e );
    }
  }

  private void linkToSubTransOutputs( TransMeta subTransMeta, IMetaverseNode subTransNode,
                                      SubtransAnalyzer<BaseStepMeta> subtransAnalyzer, String resultFieldsStep ) {
    StepNodes outputNodes = this.getOutputs();
    for ( String stepName : outputNodes.getStepNames() ) {
      // for each step after the consumer
      for ( String fieldName : outputNodes.getFieldNames( stepName ) ) {
        // for each field going to that step, link it to the field returned from the subtrans
        subtransAnalyzer.linkResultFieldToSubTrans( outputNodes.findNode( stepName, fieldName ), subTransMeta,
          subTransNode, descriptor, resultFieldsStep );
      }
    }
  }

  private void linkToSubTransInputs( Map<AnnotatedClassField<Metaverse.Node>, IMetaverseNode> externalNodes,
                                     TransMeta subTransMeta, IMetaverseNode subTransNode,
                                     SubtransAnalyzer<BaseStepMeta> subtransAnalyzer ) {
    externalNodes.entrySet().stream()
      .filter( entry -> entry.getKey().annotation.subTransLink().equals( Metaverse.SUBTRANS_INPUT ) )
      .forEach( entry ->
        subtransAnalyzer.linkUsedFieldToSubTrans( entry.getValue(), subTransMeta, subTransNode, descriptor,
          fieldName -> fieldName.equals( entry.getKey().val() ) ) );
  }


  protected String getSubTransResultFieldName() {
    String resultFieldsStep = "";
    if ( baseStepMeta instanceof BaseStreamStepMeta ) {
      resultFieldsStep = ( (BaseStreamStepMeta) baseStepMeta ).getSubStep();
    } else if ( baseStepMeta instanceof TransExecutorMeta ) {
      if ( ( (TransExecutorMeta) baseStepMeta ).getOutputRowsSourceStep() != null ) {
        resultFieldsStep = ( (TransExecutorMeta) baseStepMeta ).getOutputRowsSourceStep();
      } else if ( ( (TransExecutorMeta) baseStepMeta ).getExecutorsOutputStep() != null ) {
        resultFieldsStep = ( (TransExecutorMeta) baseStepMeta ).getExecutorsOutputStep();
      }
    }
    return resultFieldsStep;
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

    AnnotatedClassFields nodeTree = new AnnotatedClassFields( meta );

    RowMeta resourceRowMeta = new RowMeta();
    new AnnotatedClassFields( meta )
      .links()
      .filter( field -> nodeTree.node( field.annotation.nodeName() ).get().annotation.link().equals( LINK_OUTPUTS ) )
      .forEach( field -> resourceRowMeta.addValueMeta( new ValueMetaNone( field.name ) ) );

    if ( resourceRowMeta.size() > 0 ) {
      rowMetas.put( RESOURCE, resourceRowMeta );
    }
    return rowMetas;
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
    IMetaverseNode resourceFieldNode = null;

    // see if the link is pointing to an input/output field node
    StepNodes stepNodes = isOutLink( nodeLink ) ? getOutputs() : getInputs();
    if ( stepNodes != null && !stepNodes.getFieldNames().isEmpty() ) {
      resourceFieldNode = stepNodes.findNode( RESOURCE, nodeLink.nodeName() );
    }

    if ( resourceFieldNode == null ) {
      // link is probably to a resource node that isn't an input or output field
      final StepNodes newStepNodeObj = new StepNodes();
      resourceNodes.entrySet().stream()
        .filter( mapEntry -> mapEntry.getValue().getProperty( PROPERTY_TYPE ).equals( RESOURCE ) )
        .forEach( mapEntry -> newStepNodeObj.addNode( RESOURCE, mapEntry.getValue().getName(), mapEntry.getValue() ) );
      stepNodes = newStepNodeObj;

      resourceFieldNode = stepNodes.findNode( RESOURCE, nodeLink.nodeName() );
    }

    IMetaverseNode childNode = resourceFieldNode;

    if ( childNode != null ) {
      annoFields.node( nodeLink.parentNodeName() )
        .map( resourceNodes::get )
        .ifPresent( parentNode -> addLink( nodeLink, childNode, parentNode ) );
    } else {
      String parentNodeLink = nodeLink.parentNodelink();
      String fromNode = nodeLink.nodeName();
      String toNode = nodeLink.parentNodeName();
      log.warn( Messages.getErrorString( "ERROR.AnnotationDrivenStepMetaAnalyzer.LinkError",
        parentNodeLink, fromNode, toNode ) );
    }
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
  void loadStreamFields( BaseStepMeta meta ) {
    loadInputAndOutputStreamFields( meta );
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

  @VisibleForTesting
  protected SubtransAnalyzer<BaseStepMeta> getSubtransAnalyzer() {
    return new SubtransAnalyzer<>( this, log );
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

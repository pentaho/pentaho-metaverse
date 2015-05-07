/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 *
 */

package com.pentaho.metaverse.api.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IAnalysisContext;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.IMetaverseObjectFactory;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.Namespace;
import com.pentaho.metaverse.api.StepField;
import com.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class StepAnalyzer<T extends BaseStepMeta> extends BaseStepAnalyzer<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger( StepAnalyzer.class );
  public static final String NONE = "_none_";

  protected IComponentDescriptor descriptor;
  private StepNodes inputs;
  private StepNodes outputs;

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, T meta )
    throws MetaverseAnalyzerException {

    setDescriptor( descriptor );
    baseStepMeta = meta;

    validateState( descriptor, meta );

    // Add yourself
    rootNode = createNodeFromDescriptor( descriptor );
    String stepType = null;
    try {
      stepType =
        PluginRegistry.getInstance().findPluginWithId( StepPluginType.class, parentStepMeta.getStepID() ).getName();
    } catch ( Throwable t ) {
      stepType = parentStepMeta.getStepID();
    }
    rootNode.setProperty( "stepType", stepType );
    rootNode.setProperty( "copies", meta.getParentStepMeta().getCopies() );
    rootNode.setProperty( "_analyzer", this.getClass().getSimpleName() );
    metaverseBuilder.addNode( rootNode );

    // Add connection nodes
    addConnectionNodes( descriptor );

    inputs = processInputs( meta );
    outputs = processOutputs( meta );

    Set<StepField> usedFields = getUsedFields( meta );
    if ( CollectionUtils.isNotEmpty( usedFields ) ) {
      processUsedFields( usedFields );
    }

    Set<ComponentDerivationRecord> changes = getChanges();
    for ( ComponentDerivationRecord change : changes ) {
      mapChange( change );
    }

    customAnalyze( meta, rootNode );

    return rootNode;

  }

  protected void processUsedFields( Set<StepField> usedFields ) {
    for ( StepField usedField : usedFields ) {
      IMetaverseNode usedNode = getInputs().findNode( usedField );
      if ( usedNode != null ) {
        getMetaverseBuilder().addLink( rootNode, DictionaryConst.LINK_USES, usedNode );
      }
    }
  }

  protected abstract Set<StepField> getUsedFields( T meta );

  protected abstract void customAnalyze( T meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException;

  /**
   * Get all of the changes that need to be made to the metaverse. These are all of the
   * "field---derives--->field" changes
   * @return
   */
  protected Set<ComponentDerivationRecord> getChanges() {
    Set<ComponentDerivationRecord> changes = new HashSet<>();
    try {
      Set<ComponentDerivationRecord> changeRecords = getChangeRecords( baseStepMeta );
      if ( CollectionUtils.isNotEmpty( changeRecords ) ) {
        changes.addAll( changeRecords );
      }
    } catch ( MetaverseAnalyzerException e ) {
      LOGGER.warn( "Error getting change records", e );
    }

    Set<ComponentDerivationRecord> passthroughChanges = getPassthroughChanges();
    if ( CollectionUtils.isNotEmpty( passthroughChanges ) ) {
      changes.addAll( passthroughChanges );
    }
    return changes;
  }

  /**
   * Get ComponentDerivationRecords for each of the fields considered to be a passthrough
   * @return
   */
  protected Set<ComponentDerivationRecord> getPassthroughChanges() {
    Set<ComponentDerivationRecord> passthroughs = new HashSet<>();
    if ( getInputs() != null ) {
      Set<StepField> incomingFieldNames = getInputs().getFieldNames();
      for ( StepField incomingFieldName : incomingFieldNames ) {
        if ( isPassthrough( incomingFieldName ) ) {
          ComponentDerivationRecord change = new ComponentDerivationRecord(
            incomingFieldName.getFieldName(), incomingFieldName.getFieldName() );
          change.setOriginalEntityStepName( incomingFieldName.getStepName() );
          passthroughs.add( change );
        }
      }
    }
    return passthroughs;
  }

  /**
   * Determines if a field is considered a passthrough field or not. If the field name in question exists in the output
   * (exact match), then it is considered a passthrough.
   * @param originalFieldName
   * @return
   */
  protected boolean isPassthrough( StepField originalFieldName ) {
    if ( getOutputs() != null ) {
      Set<StepField> fieldNames = getOutputs().getFieldNames();
      for ( StepField fieldName : fieldNames ) {
        if ( fieldName.getFieldName().equals( originalFieldName.getFieldName() ) ) {
          return true;
        }
      }
    }
    return false;
  }


  /**
   * Add the required "derives" links to the metaverse for a ComponentDerivationRecord
   * @param change
   */
  protected void mapChange( ComponentDerivationRecord change ) {
    if ( change != null ) {
      List<IMetaverseNode> inputNodes = new ArrayList<>();
      List<IMetaverseNode> outputNodes = new ArrayList<>();

      if ( StringUtils.isNotEmpty( change.getOriginalEntityStepName() ) ) {
        inputNodes.add( getInputs().findNode( change.getOriginalField() ) );
      } else {
        inputNodes.addAll( getInputs().findNodes( change.getOriginalEntityName() ) );
      }

      if ( StringUtils.isNotEmpty( change.getChangedEntityStepName() ) ) {
        outputNodes.add( getOutputs().findNode( change.getChangedField() ) );
      } else {
        outputNodes.addAll( getOutputs().findNodes( change.getChangedEntityName() ) );
      }

      if ( CollectionUtils.isEmpty( inputNodes ) ) {
        // see if it's one of the output nodes
        inputNodes = getOutputs().findNodes( change.getOriginalEntityName() );

        // if we still don't have it, we need a transient node created
        if ( CollectionUtils.isEmpty( inputNodes ) ) {
          // create a transient node for it
          ValueMetaInterface tmp = new ValueMeta( change.getOriginalEntityName() );
          IMetaverseNode fieldNode = createOutputFieldNode( descriptor.getContext(), tmp, null, getTransientNodeType() );
          // Add link to show that this step created this as a transient field
          getMetaverseBuilder().addLink( rootNode, DictionaryConst.LINK_TRANSIENT, fieldNode );
          getMetaverseBuilder().addLink( rootNode, DictionaryConst.LINK_USES, fieldNode );
          inputNodes.add( fieldNode );
        }
      }

      if ( CollectionUtils.isEmpty( outputNodes ) ) {
        // create a transient node for it
        ValueMetaInterface tmp = new ValueMeta( change.getChangedEntityName() );
        IMetaverseNode fieldNode = createOutputFieldNode( descriptor.getContext(), tmp, null, getTransientNodeType() );
        // Add link to show that this step created this as a transient field
        getMetaverseBuilder().addLink( rootNode, DictionaryConst.LINK_TRANSIENT, fieldNode );
        getMetaverseBuilder().addLink( rootNode, DictionaryConst.LINK_USES, fieldNode );
        outputNodes.add( fieldNode );
      }

      // no input step was defined, link all field name matches together, regardless of origin step
      for ( IMetaverseNode inputNode : inputNodes ) {
        for ( IMetaverseNode outputNode : outputNodes ) {
          if ( change.getOperations().size() > 0 ) {
            outputNode.setProperty( DictionaryConst.PROPERTY_OPERATIONS, change.getOperations().toString() );
          }
          linkChangeNodes( inputNode, outputNode );
        }
      }

    }
  }

  protected void linkChangeNodes( IMetaverseNode inputNode, IMetaverseNode outputNode ) {
    getMetaverseBuilder().addLink( inputNode, getInputToOutputLinkLabel(), outputNode );
  }

  /**
   * Add new nodes to the metaverse for each of the fields that are output from this step. The fields are uniquely
   * identified based on the step that created the node and the intended target step.
   * @param meta
   * @return
   */
  protected StepNodes processOutputs( T meta ) {
    StepNodes outputs = new StepNodes();

    Map<String, RowMetaInterface> outputRowMetaInterfaces = getOutputRowMetaInterfaces( meta );
    if ( MapUtils.isNotEmpty( outputRowMetaInterfaces ) ) {
      for ( Map.Entry<String, RowMetaInterface> entry : outputRowMetaInterfaces.entrySet() ) {
        String nextStepName = entry.getKey();
        RowMetaInterface outputFields = entry.getValue();
        if ( outputFields != null ) {
          for ( ValueMetaInterface valueMetaInterface : outputFields.getValueMetaList() ) {

            IMetaverseNode fieldNode = createOutputFieldNode(
              descriptor.getContext(),
              valueMetaInterface,
              nextStepName,
              getOutputNodeType() );
            // Add link to show that this step created the field
            getMetaverseBuilder().addLink( rootNode, DictionaryConst.LINK_OUTPUTS, fieldNode );
            outputs.addNode( nextStepName, valueMetaInterface.getName(), fieldNode );
          }
        } else {
          LOGGER.warn( "No output fields found for step " + getStepName() );
        }
      }
    }
    return outputs;
  }

  protected IMetaverseNode createInputFieldNode( String previousStepName, String fieldName, int fieldType ) {
    IComponentDescriptor prevFieldDescriptor = getPrevFieldDescriptor( previousStepName, fieldName );
    ValueMetaInterface vmi = new ValueMeta( fieldName, fieldType );
    return createFieldNode( prevFieldDescriptor, vmi, getStepName(), false );
  }

  protected IMetaverseNode createOutputFieldNode( IAnalysisContext context, ValueMetaInterface fieldMeta,
                                                  String targetStepName, String nodeType ) {
    IComponentDescriptor fieldDescriptor =
      new MetaverseComponentDescriptor( fieldMeta.getName(), nodeType, rootNode, context );
    return createFieldNode( fieldDescriptor, fieldMeta, targetStepName, true );
  }

  @Override
  protected IMetaverseNode createNodeFromDescriptor( IComponentDescriptor descriptor ) {
    return super.createNodeFromDescriptor( descriptor );
  }

  protected IMetaverseNode createFieldNode( IComponentDescriptor fieldDescriptor, ValueMetaInterface fieldMeta,
                                            String targetStepName, boolean addTheNode ) {

    IMetaverseNode newFieldNode = createNodeFromDescriptor( fieldDescriptor );
    newFieldNode.setProperty( DictionaryConst.PROPERTY_KETTLE_TYPE, fieldMeta.getTypeDesc() );

    // don't add it to the graph if it is a transient node
    if ( targetStepName != null ) {
      newFieldNode.setProperty( DictionaryConst.PROPERTY_TARGET_STEP, targetStepName );
      newFieldNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_TARGET_AWARE );
      if ( addTheNode ) {
        getMetaverseBuilder().addNode( newFieldNode );
      }
    }

    return newFieldNode;
  }


  /**
   * Add links to nodes in the metaverse for each of the fields that are input into this step. The fields are uniquely
   * identified based on the step that created the node and the intended target step.
   * @param meta
   * @return
   */
  protected StepNodes processInputs( T meta ) {
    StepNodes inputs = new StepNodes();

    // get all input steps
    String[] prevStepNames = parentTransMeta.getPrevStepNames( parentStepMeta );

    if ( ArrayUtils.isNotEmpty( prevStepNames ) ) {
      Map<String, RowMetaInterface> inputFields = getInputFields( meta );
      for ( int i = 0; i < prevStepNames.length; i++ ) {
        String prevStepName = prevStepNames[ i ];
        RowMetaInterface rmi = inputFields.get( prevStepName );
        if ( rmi != null ) {
          String[] fieldNames = rmi.getFieldNames();
          for ( int j = 0; j < fieldNames.length; j++ ) {
            String fieldName = fieldNames[ j ];
            int type = rmi.getValueMeta( j ).getType();
            IMetaverseNode prevFieldNode = createInputFieldNode( prevStepName, fieldName, type );
            getMetaverseBuilder().addLink( prevFieldNode, DictionaryConst.LINK_INPUTS, rootNode );
            inputs.addNode( prevStepName, fieldName, prevFieldNode );
          }
        }
      }
    }
    return inputs;
  }

  /**
   * Create a new IComponentDescriptor for a field input into this step
   * @param prevStepName
   * @param fieldName
   * @return
   */
  protected IComponentDescriptor getPrevFieldDescriptor( String prevStepName, String fieldName ) {
    IComponentDescriptor prevFieldDescriptor = null;
    if ( StringUtils.isNotEmpty( prevStepName ) ) {
      Object nsObj = rootNode.getProperty( DictionaryConst.PROPERTY_NAMESPACE );
      INamespace ns = new Namespace( nsObj != null ? nsObj.toString() : null );
      IMetaverseNode tmpOriginNode =
        getMetaverseObjectFactory().createNodeObject( ns, prevStepName, DictionaryConst.NODE_TYPE_TRANS_STEP );

      INamespace stepFieldNamespace = new Namespace( tmpOriginNode.getLogicalId() );

      prevFieldDescriptor =
        new MetaverseComponentDescriptor( fieldName, getInputNodeType(), stepFieldNamespace,
          descriptor.getContext() );

    }
    return prevFieldDescriptor;
  }

  public String getStepName() {
    return parentStepMeta.getName();
  }

  public StepNodes getInputs() {
    return inputs;
  }

  public StepNodes getOutputs() {
    return outputs;
  }

  public void setDescriptor( IComponentDescriptor descriptor ) {
    this.descriptor = descriptor;
  }

  public Set<StepField> createStepFields( String fieldName, StepNodes stepNodes ) {
    Set<StepField> fields = new HashSet<>();
    for ( String stepName : stepNodes.getStepNames() ) {
      fields.add( new StepField( stepName, fieldName ) );
    }
    return fields;
  }

  protected String getInputToOutputLinkLabel() {
    return DictionaryConst.LINK_DERIVES;
  }

  protected String getInputNodeType() {
    return DictionaryConst.NODE_TYPE_TRANS_FIELD;
  }

  protected String getOutputNodeType() {
    return DictionaryConst.NODE_TYPE_TRANS_FIELD;
  }

  protected String getTransientNodeType() {
    return DictionaryConst.NODE_TYPE_TRANS_FIELD;
  }

  protected Map<String, RowMetaInterface> getOutputRowMetaInterfaces( T meta ) {
    String[] nextStepNames = parentTransMeta.getNextStepNames( parentStepMeta );
    Map<String, RowMetaInterface> outputRows = new HashMap<>();
    RowMetaInterface outputFields = getOutputFields( meta );

    if ( outputFields != null && ArrayUtils.isEmpty( nextStepNames ) ) {
      nextStepNames = new String[] { NONE };
    }
    for ( String stepName : nextStepNames ) {
      outputRows.put( stepName, outputFields );
    }
    return outputRows;
  }

  protected IMetaverseObjectFactory getMetaverseObjectFactory() {
    return super.metaverseObjectFactory;
  }

  protected void setMetaverseObjectFactory( IMetaverseObjectFactory factory ) {
    metaverseObjectFactory = factory;
  }

}

/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.ProgressNullMonitorListener;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IConnectionAnalyzer;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.BaseKettleMetaverseComponent;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.messages.Messages;
import org.pentaho.metaverse.api.model.kettle.IFieldMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class StepAnalyzer<T extends BaseStepMeta> extends BaseKettleMetaverseComponent implements
  IStepAnalyzer<T>, IFieldLineageMetadataProvider<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger( StepAnalyzer.class );
  public static final String NONE = "_none_";

  protected IComponentDescriptor descriptor;
  private StepNodes inputs;
  private StepNodes outputs;
  protected String[] prevStepNames = null;

  /**
   * A reference to the step under analysis
   */
  protected T baseStepMeta = null;

  /**
   * The step's parent StepMeta object (to get the parent TransMeta, in/out fields, etc.)
   */
  protected StepMeta parentStepMeta = null;

  /**
   * A reference to the transformation that contains the step under analysis
   */
  protected TransMeta parentTransMeta = null;

  /**
   * A reference to the root node created by the analyzer (usually corresponds to the step under analysis)
   */
  protected IMetaverseNode rootNode = null;

  /**
   * A reference to a connection analyzer
   */
  protected IConnectionAnalyzer connectionAnalyzer = null;

  /**
   * The stream fields coming into the step
   */
  protected Map<String, RowMetaInterface> prevFields = null;

  /**
   * The stream fields coming out of the step
   */
  protected RowMetaInterface stepFields = null;

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, T meta ) throws MetaverseAnalyzerException {

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
    rootNode.setProperty( "pluginId", parentStepMeta.getStepID() );
    rootNode.setProperty( "stepType", stepType );
    rootNode.setProperty( "copies", meta.getParentStepMeta().getCopies() );
    rootNode.setProperty( "_analyzer", this.getClass().getSimpleName() );
    metaverseBuilder.addNode( rootNode );

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
   * Get all of the changes that need to be made to the metaverse. These are all of the "field---derives--->field"
   * changes
   *
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
   *
   * @return
   */
  protected Set<ComponentDerivationRecord> getPassthroughChanges() {
    Set<ComponentDerivationRecord> passthroughs = new HashSet<>();
    if ( getInputs() != null ) {
      Set<StepField> incomingFieldNames = getInputs().getFieldNames();
      for ( StepField incomingFieldName : incomingFieldNames ) {
        if ( isPassthrough( incomingFieldName ) ) {
          ComponentDerivationRecord change =
            new ComponentDerivationRecord( incomingFieldName.getFieldName(), incomingFieldName.getFieldName() );
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
   *
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
   *
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
          IMetaverseNode fieldNode =
            createOutputFieldNode( getDescriptor().getContext(), tmp, null, getTransientNodeType() );
          // Add link to show that this step created this as a transient field
          getMetaverseBuilder().addLink( rootNode, DictionaryConst.LINK_TRANSIENT, fieldNode );
          getMetaverseBuilder().addLink( rootNode, DictionaryConst.LINK_USES, fieldNode );
          inputNodes.add( fieldNode );
        }
      }

      if ( CollectionUtils.isEmpty( outputNodes ) ) {
        // create a transient node for it
        ValueMetaInterface tmp = new ValueMeta( change.getChangedEntityName() );
        IMetaverseNode fieldNode =
          createOutputFieldNode( getDescriptor().getContext(), tmp, null, getTransientNodeType() );
        // Add link to show that this step created this as a transient field
        getMetaverseBuilder().addLink( rootNode, DictionaryConst.LINK_TRANSIENT, fieldNode );
        getMetaverseBuilder().addLink( rootNode, DictionaryConst.LINK_USES, fieldNode );
        outputNodes.add( fieldNode );
      }

      // no input step was defined, link all field name matches together, regardless of origin step
      for ( IMetaverseNode inputNode : inputNodes ) {
        for ( IMetaverseNode outputNode : outputNodes ) {
          if ( change.getOperations().size() > 0 ) {
            outputNode.setProperty( DictionaryConst.PROPERTY_OPERATIONS, change.toString() );
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
   *
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

            IMetaverseNode fieldNode =
              createOutputFieldNode( getDescriptor().getContext(), valueMetaInterface, nextStepName,
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

  protected IMetaverseNode createInputFieldNode( IAnalysisContext context, ValueMetaInterface fieldMeta,
                                                 String previousStepName, String nodeType ) {
    IComponentDescriptor prevFieldDescriptor = getPrevFieldDescriptor( previousStepName, fieldMeta.getName() );
    return createFieldNode( prevFieldDescriptor, fieldMeta, getStepName(), false );
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
   *
   * @param meta
   * @return
   */
  protected StepNodes processInputs( T meta ) {
    StepNodes inputs = new StepNodes();

    // get all input steps
    Map<String, RowMetaInterface> inputRowMetaInterfaces = getInputRowMetaInterfaces( meta );

    if ( MapUtils.isNotEmpty( inputRowMetaInterfaces ) ) {
      for ( Map.Entry<String, RowMetaInterface> entry : inputRowMetaInterfaces.entrySet() ) {
        String prevStepName = entry.getKey();
        RowMetaInterface inputFields = entry.getValue();
        if ( inputFields != null ) {
          for ( ValueMetaInterface valueMetaInterface : inputFields.getValueMetaList() ) {

            IMetaverseNode prevFieldNode =
              createInputFieldNode( getDescriptor().getContext(), valueMetaInterface, prevStepName,
                getInputNodeType() );
            getMetaverseBuilder().addLink( prevFieldNode, DictionaryConst.LINK_INPUTS, rootNode );
            inputs.addNode( prevStepName, valueMetaInterface.getName(), prevFieldNode );
          }
        } else {
          LOGGER.warn( "No input fields found for step " + getStepName() );
        }
      }
    }

    return inputs;
  }

  /**
   * Create a new IComponentDescriptor for a field input into this step
   *
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
        new MetaverseComponentDescriptor( fieldName, getInputNodeType(), stepFieldNamespace, getDescriptor()
          .getContext() );

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

  public IComponentDescriptor getDescriptor() {
    return descriptor;
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
      nextStepNames = new String[]{ NONE };
    }
    for ( String stepName : nextStepNames ) {
      outputRows.put( stepName, outputFields );
    }
    return outputRows;
  }

  protected Map<String, RowMetaInterface> getInputRowMetaInterfaces( T meta ) {
    Map<String, RowMetaInterface> inputFields = getInputFields( meta );
    return inputFields;
  }

  protected IMetaverseObjectFactory getMetaverseObjectFactory() {
    return super.metaverseObjectFactory;
  }

  protected void setMetaverseObjectFactory( IMetaverseObjectFactory factory ) {
    metaverseObjectFactory = factory;
  }

  public void validateState( IComponentDescriptor descriptor, T object ) throws MetaverseAnalyzerException {
    baseStepMeta = object;
    if ( baseStepMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.StepMetaInterface.IsNull" ) );
    }

    parentStepMeta = baseStepMeta.getParentStepMeta();
    if ( parentStepMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.StepMeta.IsNull" ) );
    }

    parentTransMeta = parentStepMeta.getParentTransMeta();

    if ( parentTransMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.ParentTransMeta.IsNull" ) );
    }

    if ( metaverseBuilder == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseBuilder.IsNull" ) );
    }

    if ( metaverseObjectFactory == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseObjectFactory.IsNull" ) );
    }
  }

  public IConnectionAnalyzer getConnectionAnalyzer() {
    return connectionAnalyzer;
  }

  public void setConnectionAnalyzer( IConnectionAnalyzer connectionAnalyzer ) {
    this.connectionAnalyzer = connectionAnalyzer;
  }

  @Override
  public Map<String, RowMetaInterface> getInputFields( T meta ) {
    Map<String, RowMetaInterface> rowMeta = null;
    try {
      validateState( null, meta );
    } catch ( MetaverseAnalyzerException e ) {
      // eat it
    }
    if ( parentTransMeta != null ) {
      try {
        rowMeta = new HashMap<String, RowMetaInterface>();
        ProgressNullMonitorListener progressMonitor = new ProgressNullMonitorListener();
        prevStepNames = parentTransMeta.getPrevStepNames( parentStepMeta );
        RowMetaInterface rmi = parentTransMeta.getPrevStepFields( parentStepMeta, progressMonitor );
        progressMonitor.done();
        if ( !ArrayUtils.isEmpty( prevStepNames ) ) {
          rowMeta.put( prevStepNames[0], rmi );
        }
      } catch ( KettleStepException e ) {
        rowMeta = null;
      }
    }
    return rowMeta;
  }

  @Override
  public RowMetaInterface getOutputFields( T meta ) {
    RowMetaInterface rmi = null;
    try {
      validateState( null, meta );
    } catch ( MetaverseAnalyzerException e ) {
      // eat it
    }
    if ( parentTransMeta != null ) {
      try {
        ProgressNullMonitorListener progressMonitor = new ProgressNullMonitorListener();
        rmi = parentTransMeta.getStepFields( parentStepMeta, progressMonitor );
        progressMonitor.done();
      } catch ( KettleStepException e ) {
        rmi = null;
      }
    }
    return rmi;
  }

  @Override
  public Set<IFieldMapping> getFieldMappings( T meta ) throws MetaverseAnalyzerException {
    return null;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( T meta ) throws MetaverseAnalyzerException {
    return null;
  }

  /**
   * Loads the in/out fields for this step into member variables for use by the analyzer
   */
  public void loadInputAndOutputStreamFields( T meta ) {
    prevFields = getInputFields( meta );
    stepFields = getOutputFields( meta );
  }

  protected IComponentDescriptor getStepFieldOriginDescriptor( IComponentDescriptor descriptor, String fieldName )
    throws MetaverseAnalyzerException {

    if ( descriptor == null || stepFields == null ) {
      return null;
    }
    ValueMetaInterface vmi = stepFields.searchValueMeta( fieldName );
    String origin = ( vmi == null ) ? fieldName : vmi.getOrigin();

    // if we can't determine the origin, throw an exception
    if ( origin == null && !ArrayUtils.isEmpty( prevStepNames ) ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.NoOriginForField", fieldName ) );
    }

    IMetaverseNode tmpOriginNode =
      metaverseObjectFactory.createNodeObject( UUID.randomUUID().toString(), origin,
        DictionaryConst.NODE_TYPE_TRANS_STEP );
    tmpOriginNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, rootNode
      .getProperty( DictionaryConst.PROPERTY_NAMESPACE ) );
    INamespace stepFieldNamespace = new Namespace( tmpOriginNode.getLogicalId() );

    MetaverseComponentDescriptor d =
      new MetaverseComponentDescriptor( fieldName, DictionaryConst.NODE_TYPE_TRANS_FIELD, tmpOriginNode, descriptor
        .getContext() );
    return d;
  }

}

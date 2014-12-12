/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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
 */

package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.BaseKettleMetaverseComponentWithDatabases;
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.analyzer.kettle.IDatabaseConnectionAnalyzer;
import com.pentaho.metaverse.api.model.kettle.IFieldInfo;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.impl.Namespace;
import com.pentaho.metaverse.impl.model.kettle.FieldInfo;
import com.pentaho.metaverse.impl.model.kettle.FieldMapping;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * KettleBaseStepAnalyzer provides a default implementation (and generic helper methods) for analyzing PDI step to
 * gather metadata for the metaverse.
 */
public abstract class BaseStepAnalyzer<T extends BaseStepMeta>
  extends BaseKettleMetaverseComponentWithDatabases implements IStepAnalyzer<T>, IFieldLineageMetadataProvider<T> {

  /**
   * The stream fields coming into the step
   */
  protected RowMetaInterface prevFields = null;

  /**
   * The stream fields coming out of the step
   */
  protected RowMetaInterface stepFields = null;

  /**
   * A reference to the step under analysis
   */
  protected BaseStepMeta baseStepMeta = null;

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
   * A reference to the database connection analyzer
   */
  protected IDatabaseConnectionAnalyzer dbConnectionAnalyzer = null;

  protected Map<String, IMetaverseNode> dbNodes = new HashMap<String, IMetaverseNode>();

  /**
   * Analyzes a step to gather metadata (such as input/output fields, used database connections, etc.)
   *
   * @see org.pentaho.platform.api.metaverse.IAnalyzer#analyze(IMetaverseComponentDescriptor, Object)
   */
  @Override
  public IMetaverseNode analyze( IMetaverseComponentDescriptor descriptor, T object )
    throws MetaverseAnalyzerException {

    validateState( descriptor, object );

    // Add yourself
    rootNode = createNodeFromDescriptor( descriptor );
    String stepType = null;
    try {
      stepType = PluginRegistry.getInstance().findPluginWithId(
        StepPluginType.class, parentStepMeta.getStepID() ).getName();
    } catch ( Throwable t ) {
      stepType = parentStepMeta.getStepID();
    }
    rootNode.setProperty( "stepType", stepType );
    rootNode.setProperty( "copies", object.getParentStepMeta().getCopies() );
    metaverseBuilder.addNode( rootNode );

    // Add database connection nodes
    addDatabaseConnectionNodes( descriptor );

    // Interrogate API to see what default field information is available
    loadInputAndOutputStreamFields();
    addCreatedFieldNodes( descriptor );
    addDeletedFieldLinks( descriptor );
    return rootNode;
  }

  /**
   * Adds any used database connections to the metaverse using the appropriate analyzer
   *
   * @throws MetaverseAnalyzerException
   */
  protected void addDatabaseConnectionNodes( IMetaverseComponentDescriptor descriptor )
    throws MetaverseAnalyzerException {

    if ( baseStepMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.StepMetaInterface.IsNull" ) );
    }

    // Analyze the database connections
    DatabaseMeta[] dbs = baseStepMeta.getUsedDatabaseConnections();
    if ( dbs != null ) {
      Set<IDatabaseConnectionAnalyzer> dbAnalyzers = getDatabaseConnectionAnalyzers();
      if ( dbAnalyzers != null ) {
        for ( IDatabaseConnectionAnalyzer dbAnalyzer : dbAnalyzers ) {
          if ( dbAnalyzer != null ) {
            dbAnalyzer.setMetaverseBuilder( metaverseBuilder );
            for ( DatabaseMeta db : dbs ) {
              try {
                IMetaverseComponentDescriptor dbDescriptor = new MetaverseComponentDescriptor(
                  db.getName(),
                  DictionaryConst.NODE_TYPE_DATASOURCE,
                  descriptor.getNamespace(),
                  descriptor.getContext() );
                IMetaverseNode dbNode = dbAnalyzer.analyze( dbDescriptor, db );
                // save the nodes we add for later use in namespacing database related things
                dbNodes.put( db.getName(), dbNode );
                metaverseBuilder.addLink( dbNode, DictionaryConst.LINK_DEPENDENCYOF, rootNode );
              } catch ( Throwable t ) {
                // Don't throw the exception if a DB connection couldn't be analyzed, just log it and move on
                t.printStackTrace( System.err );
              }
            }
          }
        }
      }
    }
  }

  /**
   * Adds to the metaverse any fields created by this step
   */
  protected void addCreatedFieldNodes( IMetaverseComponentDescriptor descriptor ) {
    try {
      if ( stepFields != null ) {
        // Find fields that were created by this step
        List<ValueMetaInterface> outRowValueMetas = stepFields.getValueMetaList();
        if ( outRowValueMetas != null ) {
          for ( ValueMetaInterface outRowMeta : outRowValueMetas ) {
            if ( prevFields != null && prevFields.searchValueMeta( outRowMeta.getName() ) == null ) {
              // This field didn't come into the step, so assume it has been created here
              IMetaverseComponentDescriptor fieldDescriptor = new MetaverseComponentDescriptor( outRowMeta.getName(),
                DictionaryConst.NODE_TYPE_TRANS_FIELD, rootNode, descriptor.getContext() );

              IMetaverseNode newFieldNode = createNodeFromDescriptor( fieldDescriptor );
              newFieldNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, rootNode.getLogicalId() );
              newFieldNode.setProperty( DictionaryConst.PROPERTY_KETTLE_TYPE, outRowMeta.getTypeDesc() );
              metaverseBuilder.addNode( newFieldNode );

              // Add link to show that this step created the field
              metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_CREATES, newFieldNode );
            }
            // no else clause: if we can't determine the fields, we can't do anything else
          }
        }
      }
    } catch ( Throwable t ) {
      // TODO Don't throw an exception here, just log the error and move on
      t.printStackTrace( System.err );
    }
  }

  /**
   * Adds to the metaverse links to fields that are input to a step but not output from the step
   */
  protected void addDeletedFieldLinks( IMetaverseComponentDescriptor descriptor ) {
    try {
      if ( prevFields != null ) {
        List<ValueMetaInterface> inRowValueMetas = prevFields.getValueMetaList();
        if ( inRowValueMetas != null ) {
          for ( ValueMetaInterface inRowMeta : inRowValueMetas ) {
            // Find fields that were deleted by this step
            if ( stepFields != null && stepFields.searchValueMeta( inRowMeta.getName() ) == null ) {
              // This field didn't leave the step, so assume it has been deleted here
              IMetaverseComponentDescriptor fieldDescriptor =
                getPrevStepFieldOriginDescriptor( descriptor, inRowMeta.getName() );
              IMetaverseNode inFieldNode = createNodeFromDescriptor( fieldDescriptor );

              // Add link to show that this step created the field
              metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_DELETES, inFieldNode );
            }
            // no else clause: if we can't determine the fields, we can't do anything else
          }
        }
      }
    } catch ( Throwable t ) {
      // TODO Don't throw an exception here, just log the error and move on
      t.printStackTrace( System.err );
    }

  }

  /**
   * Loads the in/out fields for this step into member variables for use by the analyzer
   */
  protected void loadInputAndOutputStreamFields() {
    if ( parentTransMeta != null ) {
      try {
        prevFields = parentTransMeta.getPrevStepFields( parentStepMeta );
        if ( prevFields != null && prevFields.size() > 0 ) {
          List<IFieldInfo> fieldInfos = new ArrayList<IFieldInfo>();
          List<ValueMetaInterface> valueMetaInterfaces = prevFields.getValueMetaList();
          for ( ValueMetaInterface valueMetaInterface : valueMetaInterfaces ) {
            FieldInfo fieldInfo = new FieldInfo( valueMetaInterface );
            fieldInfos.add( fieldInfo );
          }
          //          stepInfo.setInputFields( fieldInfos );
        }
      } catch ( Throwable t ) {
        prevFields = null;
      }
      try {
        stepFields = parentTransMeta.getStepFields( parentStepMeta );
        if ( stepFields != null && stepFields.size() > 0 ) {
          List<IFieldInfo> fieldInfos = new ArrayList<IFieldInfo>();
          List<ValueMetaInterface> valueMetaInterfaces = stepFields.getValueMetaList();
          for ( ValueMetaInterface valueMetaInterface : valueMetaInterfaces ) {
            FieldInfo fieldInfo = new FieldInfo( valueMetaInterface );
            fieldInfos.add( fieldInfo );
          }
          //          stepInfo.setOutputFields( fieldInfos );
        }
      } catch ( Throwable t ) {
        stepFields = null;
      }
    }
  }

  protected IMetaverseComponentDescriptor getPrevStepFieldOriginDescriptor(
    IMetaverseComponentDescriptor descriptor, String fieldName ) {
    if ( descriptor == null ) {
      return null;
    }

    ValueMetaInterface vmi = prevFields.searchValueMeta( fieldName );
    String origin = ( vmi == null ) ? fieldName : vmi.getOrigin();

    Object nsObj = rootNode.getProperty( DictionaryConst.PROPERTY_NAMESPACE );
    INamespace ns = new Namespace( nsObj != null ? nsObj.toString() : null );
    IMetaverseNode tmpOriginNode = metaverseObjectFactory.createNodeObject(
      ns,
      origin,
      DictionaryConst.NODE_TYPE_TRANS_STEP );

    INamespace stepFieldNamespace = new Namespace( tmpOriginNode.getLogicalId() );

    IMetaverseComponentDescriptor prevFieldDescriptor = new MetaverseComponentDescriptor(
      fieldName,
      DictionaryConst.NODE_TYPE_TRANS_FIELD,
      stepFieldNamespace,
      descriptor.getContext() );
    return prevFieldDescriptor;
  }

  protected IMetaverseComponentDescriptor getStepFieldOriginDescriptor(
    IMetaverseComponentDescriptor descriptor, String fieldName ) {
    if ( descriptor == null || stepFields == null ) {
      return null;
    }
    ValueMetaInterface vmi = stepFields.searchValueMeta( fieldName );
    String origin = ( vmi == null ) ? fieldName : vmi.getOrigin();

    IMetaverseNode tmpOriginNode = metaverseObjectFactory.createNodeObject( UUID.randomUUID().toString(),
      origin, DictionaryConst.NODE_TYPE_TRANS_STEP );
    tmpOriginNode.setProperty( "namespace", rootNode.getProperty( "namespace" ) );
    INamespace stepFieldNamespace = new Namespace( tmpOriginNode.getLogicalId() );

    MetaverseComponentDescriptor d = new MetaverseComponentDescriptor( fieldName, DictionaryConst.NODE_TYPE_TRANS_FIELD,
      tmpOriginNode, descriptor.getContext() );
    return d;
  }

  /**
   * Checks for the validity/presence of objects used internally in step analysis, such as the reference to the
   * metaverse builder.
   *
   * @param descriptor the descriptor for the object argument
   * @param object     the object being analyzed
   * @throws MetaverseAnalyzerException if the state of the internal objects is not valid
   */
  protected void validateState( IMetaverseComponentDescriptor descriptor, T object ) throws MetaverseAnalyzerException {
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

  /**
   * Processes the given field changes, applying them to the metaverse. This method returns a metaverse node
   * corresponding to the derived field, but does not add it to the metaverse.
   *
   * @param descriptor   the descriptor for the field
   * @param fieldNode    the original field's metaverse node
   * @param changeRecord the record of changes made to the field
   * @return a metaverse node corresponding to the derived stream field.
   */
  protected IMetaverseNode processFieldChangeRecord(
    IMetaverseComponentDescriptor descriptor,
    IMetaverseNode fieldNode,
    ComponentDerivationRecord changeRecord ) {

    IMetaverseNode newFieldNode = null;

    // There should be at least one operation in order to create a new stream field
    if ( changeRecord != null && changeRecord.hasDelta() && descriptor != null ) {
      // Create a new node for the renamed field
      IMetaverseComponentDescriptor newFieldDescriptor = new MetaverseComponentDescriptor(
        changeRecord.getChangedEntityName(),
        DictionaryConst.NODE_TYPE_TRANS_FIELD,
        new Namespace( rootNode.getLogicalId() ),
        descriptor.getContext() );
      newFieldNode = createNodeFromDescriptor( newFieldDescriptor );
      newFieldNode.setProperty( DictionaryConst.PROPERTY_OPERATIONS, changeRecord.toString() );
      metaverseBuilder.addLink( fieldNode, DictionaryConst.LINK_DERIVES, newFieldNode );
    }
    return newFieldNode;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( T meta ) throws MetaverseAnalyzerException {
    return null;
  }

  /**
   * Override this to provide field mappings. Otherwise no field mappings can be assumed.
   *
   * @param meta
   * @return null
   * @throws MetaverseAnalyzerException
   */
  @Override
  public Set<IFieldMapping> getFieldMappings( T meta ) throws MetaverseAnalyzerException {
    return null;
  }

  public Set<IFieldMapping> getPassthruFieldMappings( T meta ) throws MetaverseAnalyzerException {
    Set<IFieldMapping> mappings = new HashSet<IFieldMapping>();
    // inputs map directly to outputs
    if ( prevFields == null ) {
      this.validateState( null, meta );
      loadInputAndOutputStreamFields();
      if ( prevFields == null ) {
        // either not connected or there aren't any fields coming into the step so there are no mappings
        return mappings;
      }
    }
    for ( String field : prevFields.getFieldNames() ) {
      mappings.add( new FieldMapping( field, field ) );
    }
    return mappings;
  }

}

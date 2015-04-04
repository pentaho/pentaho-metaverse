/*!
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
 */

package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.BaseKettleMetaverseComponent;
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.api.IConnectionAnalyzer;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.Namespace;
import com.pentaho.metaverse.api.model.kettle.FieldMapping;
import com.pentaho.metaverse.messages.Messages;
import org.apache.commons.lang.ArrayUtils;
import org.pentaho.di.core.ProgressNullMonitorListener;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import com.pentaho.metaverse.api.IAnalysisContext;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
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
public abstract class BaseStepAnalyzer<T extends BaseStepMeta> extends BaseKettleMetaverseComponent
  implements IStepAnalyzer<T>, IFieldLineageMetadataProvider<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger( BaseStepAnalyzer.class );

  /**
   * The stream fields coming into the step
   */
  protected Map<String, RowMetaInterface> prevFields = null;

  protected String[] prevStepNames = null;

  /**
   * The stream fields coming out of the step
   */
  protected RowMetaInterface stepFields = null;

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

  protected Map<String, IMetaverseNode> dbNodes = new HashMap<String, IMetaverseNode>();

  /**
   * A reference to a connection analyzer
   */
  protected IConnectionAnalyzer connectionAnalyzer = null;

  /**
   * Analyzes a step to gather metadata (such as input/output fields, used database connections, etc.)
   *
   * @see com.pentaho.metaverse.api.IAnalyzer#analyze(com.pentaho.metaverse.api.IComponentDescriptor,
   * Object)
   */
  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, T object ) throws MetaverseAnalyzerException {

    validateState( descriptor, object );

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
    rootNode.setProperty( "copies", object.getParentStepMeta().getCopies() );
    metaverseBuilder.addNode( rootNode );

    // Add connection nodes
    addConnectionNodes( descriptor );

    // Interrogate API to see what default field information is available
    loadInputAndOutputStreamFields( object );
    addCreatedFieldNodes( descriptor );
    addDeletedFieldLinks( descriptor );
    return rootNode;
  }

  /**
   * Adds any used connections to the metaverse using the appropriate analyzer
   *
   * @throws MetaverseAnalyzerException
   */
  protected void addConnectionNodes( IComponentDescriptor descriptor ) throws MetaverseAnalyzerException {

    if ( baseStepMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.StepMetaInterface.IsNull" ) );
    }

    if ( connectionAnalyzer != null ) {
      List connections = connectionAnalyzer.getUsedConnections( baseStepMeta );
      for ( Object connection : connections ) {
        String connName = null;
        // see if the connection object has a getName method
        try {
          Method getNameMethod = connection.getClass().getMethod( "getName", null );
          connName = (String) getNameMethod.invoke( connection, null );
        } catch ( Exception e ) {
          // doesn't have a getName method, will try to get it from the descriptor later
        }
        try {
          IComponentDescriptor connDescriptor = connectionAnalyzer.buildComponentDescriptor( descriptor, connection );
          connName = connName == null ? descriptor.getName() : connName;
          IMetaverseNode connNode = (IMetaverseNode) connectionAnalyzer.analyze( connDescriptor, connection );
          dbNodes.put( connDescriptor.getName(), connNode );
          metaverseBuilder.addLink( connNode, DictionaryConst.LINK_DEPENDENCYOF, rootNode );
        } catch ( Throwable t ) {
          // Don't throw the exception if a DB connection couldn't be analyzed, just log it and move on
          LOGGER.warn( Messages.getString( "WARNING.AnalyzingDatabaseConnection", connName ), t );
        }
      }
    }
  }

  /**
   * Adds to the metaverse any fields created by this step
   */
  protected void addCreatedFieldNodes( IComponentDescriptor descriptor ) {
    try {
      if ( stepFields != null ) {
        // Find fields that were created by this step
        List<ValueMetaInterface> outRowValueMetas = stepFields.getValueMetaList();
        if ( outRowValueMetas != null ) {
          for ( ValueMetaInterface outRowMeta : outRowValueMetas ) {
            if ( !fieldNameExistsInInput( outRowMeta.getName() ) ) {
              // This field didn't come into the step, so assume it has been created here
              createFieldNode( descriptor.getContext(), outRowMeta );
            }
            // no else clause: if we can't determine the fields, we can't do anything else
          }
        }
      }
    } catch ( Throwable t ) {
      // TODO Don't throw an exception here, just log the error and move on
      LOGGER.warn( Messages.getString( "WARNING.AddingNodesCreated" ), t );
    }
  }

  protected void createFieldNode( IAnalysisContext context, ValueMetaInterface fieldMeta ) {
    IComponentDescriptor fieldDescriptor =
      new MetaverseComponentDescriptor( fieldMeta.getName(), DictionaryConst.NODE_TYPE_TRANS_FIELD,
        rootNode, context );

    IMetaverseNode newFieldNode = createNodeFromDescriptor( fieldDescriptor );
    newFieldNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, rootNode.getLogicalId() );
    newFieldNode.setProperty( DictionaryConst.PROPERTY_KETTLE_TYPE, fieldMeta.getTypeDesc() );
    metaverseBuilder.addNode( newFieldNode );

    // Add link to show that this step created the field
    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_CREATES, newFieldNode );
  }

  protected boolean fieldNameExistsInInput( String fieldName ) {
    boolean match = false;
    if ( prevFields != null ) {
      for ( String stepName : prevFields.keySet() ) {
        RowMetaInterface rmi = prevFields.get( stepName );
        if ( rmi != null ) {
          if ( rmi.searchValueMeta( fieldName ) != null ) {
            match = true;
            break;
          }
        }
      }
    }
    return match;
  }

  /**
   * Adds to the metaverse links to fields that are input to a step but not output from the step
   */
  protected void addDeletedFieldLinks( IComponentDescriptor descriptor ) {
    if ( prevFields != null ) {
      // check all incoming fields
      for ( RowMetaInterface rowMetaInterface : prevFields.values() ) {
        try {
          // for some reason, searchValueMeta isn't always finding valid fields when it should
          List<String> outFields = Arrays.asList( stepFields.getFieldNames() );
          List<ValueMetaInterface> inRowValueMetas = rowMetaInterface.getValueMetaList();
          if ( inRowValueMetas != null ) {
            for ( ValueMetaInterface inRowMeta : inRowValueMetas ) {
              // Find fields that were deleted by this step

              if ( stepFields != null && !outFields.contains( inRowMeta.getName() ) ) {
                // This field didn't leave the step, so assume it has been deleted here
                IComponentDescriptor fieldDescriptor =
                  getPrevStepFieldOriginDescriptor( descriptor, inRowMeta.getName() );
                IMetaverseNode inFieldNode = createNodeFromDescriptor( fieldDescriptor );

                // Add link to show that this step created the field
                metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_DELETES, inFieldNode );
              }
              // no else clause: if we can't determine the fields, we can't do anything else
            }
          }
        } catch ( Throwable t ) {
          // TODO Don't throw an exception here, just log the error and move on
          LOGGER.warn( Messages.getString( "WARNING.AddingNodesRemoved" ), t );
        }
      }
    }
  }

  /**
   * Loads the in/out fields for this step into member variables for use by the analyzer
   */
  public void loadInputAndOutputStreamFields( T meta ) {
    prevFields = getInputFields( meta );
    stepFields = getOutputFields( meta );
  }

  protected IComponentDescriptor getPrevStepFieldOriginDescriptor( IComponentDescriptor descriptor, String fieldName ) {

    IComponentDescriptor componentDescriptor = null;

    // try the first step
    if ( parentTransMeta != null && prevFields != null ) {
      if ( !ArrayUtils.isEmpty( prevStepNames ) ) {
        for ( String prevStepName : prevStepNames ) {
          RowMetaInterface rmi = prevFields.get( prevStepName );
          if ( rmi != null && rmi.searchValueMeta( fieldName ) != null ) {
            componentDescriptor =
              getPrevStepFieldOriginDescriptor( descriptor, fieldName, prevFields.get( prevStepName ) );
            break;
          }
        }
        if ( componentDescriptor == null ) {
          componentDescriptor =
            getPrevStepFieldOriginDescriptor( descriptor, fieldName, prevFields.get( prevStepNames[0] ) );
        }
      } else if ( prevFields.size() > 0 ) {
        // just use one of the inputs as a last resort
        for ( String s : prevFields.keySet() ) {
          LOGGER.debug( Messages.getString( "DEBUG.FallingBackToFirstSetOfInputFields", s ) );
          componentDescriptor =
            getPrevStepFieldOriginDescriptor( descriptor, fieldName, prevFields.get( prevStepNames ) );
          break;
        }
      }
    }
    return componentDescriptor;
  }

  protected IComponentDescriptor getPrevStepFieldOriginDescriptor( IComponentDescriptor descriptor, String fieldName,
                                                                   RowMetaInterface prevFields ) {
    if ( descriptor == null || prevFields == null ) {
      return null;
    }

    ValueMetaInterface vmi = prevFields.searchValueMeta( fieldName );
    String origin = ( vmi == null ) ? fieldName : vmi.getOrigin();

    Object nsObj = rootNode.getProperty( DictionaryConst.PROPERTY_NAMESPACE );
    INamespace ns = new Namespace( nsObj != null ? nsObj.toString() : null );
    IMetaverseNode tmpOriginNode =
      metaverseObjectFactory.createNodeObject( ns, origin, DictionaryConst.NODE_TYPE_TRANS_STEP );

    INamespace stepFieldNamespace = new Namespace( tmpOriginNode.getLogicalId() );

    IComponentDescriptor prevFieldDescriptor =
      new MetaverseComponentDescriptor( fieldName, DictionaryConst.NODE_TYPE_TRANS_FIELD, stepFieldNamespace,
        descriptor.getContext() );
    return prevFieldDescriptor;
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
    tmpOriginNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, rootNode.getProperty( DictionaryConst.PROPERTY_NAMESPACE ) );
    INamespace stepFieldNamespace = new Namespace( tmpOriginNode.getLogicalId() );

    MetaverseComponentDescriptor d =
      new MetaverseComponentDescriptor( fieldName, DictionaryConst.NODE_TYPE_TRANS_FIELD, tmpOriginNode, descriptor
        .getContext() );
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

  /**
   * Processes the given field changes, applying them to the metaverse. This method returns a metaverse node
   * corresponding to the derived field, but does not add it to the metaverse.
   *
   * @param descriptor   the descriptor for the field
   * @param fieldNode    the original field's metaverse node
   * @param changeRecord the record of changes made to the field
   * @return a metaverse node corresponding to the derived stream field.
   */
  protected IMetaverseNode processFieldChangeRecord( IComponentDescriptor descriptor, IMetaverseNode fieldNode,
                                                     ComponentDerivationRecord changeRecord ) {

    IMetaverseNode newFieldNode = null;

    // There should be at least one operation in order to create a new stream field
    if ( changeRecord != null && changeRecord.hasDelta() && descriptor != null ) {
      // Create a new node for the renamed field
      IComponentDescriptor newFieldDescriptor =
        new MetaverseComponentDescriptor( changeRecord.getChangedEntityName(), DictionaryConst.NODE_TYPE_TRANS_FIELD,
          new Namespace( rootNode.getLogicalId() ), descriptor.getContext() );
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

  public Set<IFieldMapping> getPassthruFieldMappings( T meta ) throws MetaverseAnalyzerException {
    Set<IFieldMapping> mappings = new HashSet<IFieldMapping>();
    // inputs map directly to outputs
    if ( prevFields == null ) {
      this.validateState( null, meta );
      loadInputAndOutputStreamFields( meta );
    }
    // Shouldn't be null now
    if ( prevFields != null ) {
      Collection<RowMetaInterface> prevFieldValues = prevFields.values();
      if ( prevFieldValues != null ) {
        for ( RowMetaInterface rowMetaInterface : prevFieldValues ) {
          String[] fieldNames = rowMetaInterface.getFieldNames();
          if ( fieldNames != null ) {
            for ( String field : fieldNames ) {
              mappings.add( new FieldMapping( field, field ) );
            }
          }
        }
      }
    }
    return mappings;
  }

  public IConnectionAnalyzer getConnectionAnalyzer() {
    return connectionAnalyzer;
  }

  public void setConnectionAnalyzer( IConnectionAnalyzer connectionAnalyzer ) {
    this.connectionAnalyzer = connectionAnalyzer;
  }

}

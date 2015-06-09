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

package org.pentaho.metaverse.analyzer.kettle.step.transexecutor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.ProgressNullMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.rowsfromresult.RowsFromResultMeta;
import org.pentaho.di.trans.steps.rowstoresult.RowsToResultMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransExecutorStepAnalyzer extends StepAnalyzer<TransExecutorMeta> {

  public static final String TRANSFORMATION_TO_EXECUTE = "transformationToExecute";
  public static final String EXECUTION_RESULTS_TARGET = "executionResultsTarget";
  public static final String OUTPUT_ROWS_TARGET = "outputRowsTarget";
  public static final String RESULT_FILES_TARGET = "resultFilesTarget";
  private Logger log = LoggerFactory.getLogger( TransExecutorStepAnalyzer.class );


  @Override
  protected Set<StepField> getUsedFields( TransExecutorMeta meta ) {
    // add uses links to all incoming fields
    return getInputs().getFieldNames();
  }

  @Override
  protected boolean isPassthrough( StepField originalFieldName ) {
    // there are no passthrough fields
    return false;
  }

  @Override
  protected void customAnalyze( TransExecutorMeta meta, IMetaverseNode node )
    throws MetaverseAnalyzerException {

    String transPath = meta.getFileName();
    TransMeta subTransMeta = null;
    Repository repo = parentTransMeta.getRepository();

    switch ( meta.getSpecificationMethod() ) {
      case FILENAME:
        transPath = parentTransMeta.environmentSubstitute( meta.getFileName() );
        try {
          String normalized = KettleAnalyzerUtil.normalizeFilePath( transPath );

          subTransMeta = getSubTransMeta( normalized );
          transPath = normalized;

        } catch ( Exception e ) {
          log.error( e.getMessage(), e );
          throw new MetaverseAnalyzerException( "Sub transformation can not be found - " + transPath, e );
        }
        break;
      case REPOSITORY_BY_NAME:
        if ( repo != null ) {
          String dir = parentTransMeta.environmentSubstitute( meta.getDirectoryPath() );
          String file = parentTransMeta.environmentSubstitute( meta.getTransName() );
          try {
            RepositoryDirectoryInterface rdi = repo.findDirectory( dir );
            subTransMeta = repo.loadTransformation( file, rdi, null, true, null );
            transPath = subTransMeta.getPathAndName() + "." + subTransMeta.getDefaultExtension();
          } catch ( KettleException e ) {
            log.error( e.getMessage(), e );
            throw new MetaverseAnalyzerException( "Sub transformation can not be found in repository - " + file, e );
          }
        } else {
          throw new MetaverseAnalyzerException( "Not connected to a repository, can't get the transformation" );
        }
        break;
      case REPOSITORY_BY_REFERENCE:
        if ( repo != null ) {
          try {
            subTransMeta = repo.loadTransformation( meta.getTransObjectId(), null );
            transPath = subTransMeta.getPathAndName() + "." + subTransMeta.getDefaultExtension();
          } catch ( KettleException e ) {
            log.error( e.getMessage(), e );
            throw new MetaverseAnalyzerException( "Sub transformation can not be found by reference - "
              + meta.getTransObjectId(), e );
          }
        } else {
          throw new MetaverseAnalyzerException( "Not connected to a repository, can't get the transformation" );
        }
        break;
    }

    // analyze the sub trans?

    IComponentDescriptor ds = new MetaverseComponentDescriptor(
      subTransMeta.getName(),
      DictionaryConst.NODE_TYPE_TRANS,
      descriptor.getNamespace().getParentNamespace() );

    IMetaverseNode transformationNode = createNodeFromDescriptor( ds );
    transformationNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, ds.getNamespaceId() );
    transformationNode.setProperty( DictionaryConst.PROPERTY_PATH, transPath );
    transformationNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

    metaverseBuilder.addLink( node, DictionaryConst.LINK_EXECUTES, transformationNode );

    connectToSubTransInputFields( meta, subTransMeta, transformationNode, descriptor );
    connectToSubTransOutputFields( meta, subTransMeta, transformationNode, descriptor );

    node.setProperty( TRANSFORMATION_TO_EXECUTE, transPath );

    if ( StringUtils.isNotEmpty( meta.getExecutionResultTargetStep() ) ) {
      node.setProperty( EXECUTION_RESULTS_TARGET, meta.getExecutionResultTargetStep() );
    }

    if ( StringUtils.isNotEmpty( meta.getOutputRowsSourceStep() ) ) {
      node.setProperty( OUTPUT_ROWS_TARGET, meta.getOutputRowsSourceStep() );
    }

    if ( StringUtils.isNotEmpty( meta.getResultFilesTargetStep() ) ) {
      node.setProperty( RESULT_FILES_TARGET, meta.getResultFilesTargetStep() );
    }

  }

  protected TransMeta getSubTransMeta( String filePath )
    throws FileNotFoundException, KettleXMLException, KettleMissingPluginsException {
    FileInputStream fis = new FileInputStream( filePath );
    return new TransMeta( fis, null, true, null, null );
  }

  protected void connectToSubTransOutputFields( TransExecutorMeta meta, TransMeta subTransMeta,
                                                IMetaverseNode subTransNode,
                                                IComponentDescriptor descriptor ) {

    if ( meta.getOutputRowsSourceStep() != null ) {
      String outputStep = meta.getOutputRowsSourceStep();

      for ( int i = 0; i < meta.getOutputRowsField().length; i++ ) {
        String fieldName = meta.getOutputRowsField()[ i ];
        IMetaverseNode outNode = getOutputs().findNode( outputStep, fieldName );
        if ( outNode != null ) {
          // add link, if needed, from sub tran result fields to the node just created
          linkResultFieldToSubTrans( outNode, subTransMeta, subTransNode, descriptor );
        }
      }
    }
  }

  /**
   * Checks to see if the sub trans has any RowToResult steps in it.
   * If so, it will link the fields it outputs to the fields created by this step and are sent to the
   * "target step for output rows".
   *
   * @param streamFieldNode stream field node sent to the step defined as "target step for output rows"
   * @param subTransMeta TransMeta of the transformation to be executed by the TransExecutor step
   * @param subTransNode IMetaverseNode representing the sub-transformation to be executed
   * @param descriptor Descriptor to use as a basis for any nodes created
   */
  protected void linkResultFieldToSubTrans( IMetaverseNode streamFieldNode, TransMeta subTransMeta,
                                            IMetaverseNode subTransNode, IComponentDescriptor descriptor ) {

    List<StepMeta> steps = subTransMeta.getSteps();
    if ( !CollectionUtils.isEmpty( steps ) ) {
      for ( StepMeta step : steps ) {
        if ( step.getStepMetaInterface() instanceof RowsToResultMeta ) {
          RowsToResultMeta rtrm = (RowsToResultMeta) step.getStepMetaInterface();

          // Create a new descriptor for the RowsToResult step.
          IComponentDescriptor stepDescriptor = new MetaverseComponentDescriptor( step.getName(),
            DictionaryConst.NODE_TYPE_TRANS_STEP, subTransNode, descriptor.getContext() );

          // Create a new node for the step, to be used as the parent of the the field we want to link to
          IMetaverseNode subTransStepNode = createNodeFromDescriptor( stepDescriptor );

          try {
            RowMetaInterface rowMetaInterface = rtrm.getParentStepMeta().getParentTransMeta().getStepFields( step );
            for ( int i = 0; i < rowMetaInterface.getFieldNames().length; i++ ) {
              String field = rowMetaInterface.getFieldNames()[ i ];
              if ( streamFieldNode.getName().equals( field ) ) {
                // Create the descriptor for the trans field that is derived from the incoming result field
                IComponentDescriptor stepFieldDescriptor = new MetaverseComponentDescriptor( field,
                  DictionaryConst.NODE_TYPE_TRANS_FIELD, subTransStepNode, descriptor.getContext() );

                // Create the node
                //IMetaverseNode subTransField = createNodeFromDescriptor( stepFieldDescriptor );
                IMetaverseNode subTransField = createFieldNode( stepFieldDescriptor, rowMetaInterface.getValueMeta( i ),
                  StepAnalyzer.NONE, false );

                // Add the link
                metaverseBuilder.addLink( subTransField, DictionaryConst.LINK_DERIVES, streamFieldNode );

                // no need to keep looking for a match on field name, we just handled it.
                continue;

              }
            }
          } catch ( KettleStepException e ) {
            log.warn( "Could not get step fields of RowsToResult step in sub transformation - "
              + subTransMeta.getName(), e );
          }

        }
      }
    }

  }

  /**
   * Add links from all incoming stream fields to the fields in the sub transformation
   * @param meta
   * @param subTransMeta
   * @param subTransNode
   * @param descriptor
   */
  protected void connectToSubTransInputFields( TransExecutorMeta meta, TransMeta subTransMeta, IMetaverseNode subTransNode,
    IComponentDescriptor descriptor ) {

    Set<StepField> incomingFields = getInputs().getFieldNames();
    for ( StepField field : incomingFields ) {
      IMetaverseNode inputNode = getInputs().findNode( field );
      linkUsedFieldToSubTrans( inputNode, subTransMeta, subTransNode, descriptor );
    }
  }

  /**
   * Checks to see if the sub trans has any RowFromResult steps in it.
   * If so, it will link the original field node to the fields created in the RowFromResult step in the sub trans
   *
   * @param originalFieldNode incoming stream field node to the TransExecutorStep
   * @param subTransMeta TransMeta of the transformation to be executed by the TransExecutor step
   * @param subTransNode IMetaverseNode representing the sub-transformation to be executed
   * @param descriptor Descriptor to use as a basis
   */
  protected void linkUsedFieldToSubTrans( IMetaverseNode originalFieldNode, TransMeta subTransMeta,
                                          IMetaverseNode subTransNode, IComponentDescriptor descriptor ) {

    List<StepMeta> steps = subTransMeta.getSteps();
    if ( !CollectionUtils.isEmpty( steps ) ) {
      for ( StepMeta step : steps ) {
        if ( step.getStepMetaInterface() instanceof RowsFromResultMeta ) {
          RowsFromResultMeta rfrm = (RowsFromResultMeta) step.getStepMetaInterface();

          // Create a new descriptor for the RowsFromResult step.
          IComponentDescriptor stepDescriptor = new MetaverseComponentDescriptor( StepAnalyzer.NONE,
            DictionaryConst.NODE_TYPE_TRANS_STEP, subTransNode, descriptor.getContext() );

          // Create a new node for the step, to be used as the parent of the the field we want to link to
          IMetaverseNode subTransStepNode = createNodeFromDescriptor( stepDescriptor );

          try {
            RowMetaInterface rowMetaInterface = rfrm.getParentStepMeta().getParentTransMeta().getStepFields( step );
            for ( int i = 0; i < rowMetaInterface.getFieldNames().length; i++ ) {
              String field = rowMetaInterface.getFieldNames()[ i ];
              if ( originalFieldNode.getName().equals( field ) ) {
                // Create the descriptor for the trans field that is derived from the incoming result field
                IComponentDescriptor stepFieldDescriptor = new MetaverseComponentDescriptor( field,
                  DictionaryConst.NODE_TYPE_TRANS_FIELD, subTransStepNode, descriptor.getContext() );

                // Create the node
                IMetaverseNode subTransField =
                  createFieldNode( stepFieldDescriptor, rowMetaInterface.getValueMeta( i ), step.getName(), false );

                // Add the link
                metaverseBuilder.addLink( originalFieldNode, DictionaryConst.LINK_DERIVES, subTransField );

                // no need to keep looking for a match on field name, we just handled it.
                continue;
              }
            }
          } catch ( KettleStepException e ) {
            log.warn( "Could not get step fields of RowsFromResult step in sub transformation - "
              + subTransMeta.getName(), e );
          }
        }
      }
    }
  }

  @Override
  protected Map<String, RowMetaInterface> getOutputRowMetaInterfaces( TransExecutorMeta meta ) {
    Map<String, RowMetaInterface> outputFields = new HashMap<>();
    String[] nextStepNames = parentTransMeta.getNextStepNames( parentStepMeta );
    for ( int i = 0; i < nextStepNames.length; i++ ) {
      String nextStepName = nextStepNames[ i ];
      StepMeta step = parentTransMeta.findStep( nextStepName );
      ProgressNullMonitorListener progressMonitor = new ProgressNullMonitorListener();
      try {
        RowMetaInterface prevStepFields = parentTransMeta.getPrevStepFields( step, progressMonitor );
        outputFields.put( nextStepName, prevStepFields );
        progressMonitor.done();
      } catch ( KettleStepException e ) {
        log.warn( "Could not get step fields for " + nextStepName, e );
      }
    }
    return outputFields;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    HashSet<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<Class<? extends BaseStepMeta>>();
    supportedSteps.add( TransExecutorMeta.class );
    return supportedSteps;
  }

  //// Used to aid unit testing
  protected void setParentTransMeta( TransMeta parentTransMeta ) {
    this.parentTransMeta = parentTransMeta;
  }
  protected void setParentStepMeta( StepMeta parentStepMeta ) {
    this.parentStepMeta = parentStepMeta;
  }
  @Override
  protected IMetaverseNode createFieldNode( IComponentDescriptor fieldDescriptor, ValueMetaInterface fieldMeta,
                                            String targetStepName, boolean addTheNode ) {
    // need access to spy on this in the unit test, have to override it and just call up to super
    return super.createFieldNode( fieldDescriptor, fieldMeta, targetStepName, addTheNode );
  }
  ////////
}

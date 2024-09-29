/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.step.transexecutor;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.ProgressNullMonitorListener;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.SubtransAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class provides an analyzer for the "Execute Transformation" step
 */
@SuppressWarnings( { "WeakerAccess", "UnusedReturnValue" } )
public class TransExecutorStepAnalyzer extends StepAnalyzer<TransExecutorMeta> {

  static final String TRANSFORMATION_TO_EXECUTE = "transformationToExecute";
  public static final String EXECUTION_RESULTS_TARGET = "executionResultsTarget";
  public static final String OUTPUT_ROWS_TARGET = "outputRowsTarget";
  public static final String RESULT_FILES_TARGET = "resultFilesTarget";
  private Logger log = LoggerFactory.getLogger( TransExecutorStepAnalyzer.class );
  private SubtransAnalyzer<TransExecutorMeta> subtransAnalyzer;

  public TransExecutorStepAnalyzer() {
    subtransAnalyzer = new SubtransAnalyzer<>( this, log );
  }

  void setSubtransAnalyzer( SubtransAnalyzer<TransExecutorMeta> subtransAnalyzer ) {
    this.subtransAnalyzer = subtransAnalyzer;
  }

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

  protected IMetaverseNode analyzerSubTransformation( final TransExecutorMeta meta, IMetaverseNode node )
    throws MetaverseAnalyzerException {
    return KettleAnalyzerUtil.analyze( this, parentTransMeta, meta, node );
  }

  protected TransMeta getSubTransMeta( final TransExecutorMeta meta )
    throws MetaverseAnalyzerException {
    return KettleAnalyzerUtil.getSubTransMeta( meta );
  }

  @Override
  protected void customAnalyze( TransExecutorMeta meta, IMetaverseNode node )
    throws MetaverseAnalyzerException {

    final IMetaverseNode subTransNode = analyzerSubTransformation( meta, node );
    final TransMeta subTransMeta = getSubTransMeta( meta );

    String transformationPath = KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta );
    if ( transformationPath != null ) {
      transformationPath = parentTransMeta.environmentSubstitute( transformationPath );
    }

    connectToSubTransInputFields( subTransMeta, subTransNode, descriptor );
    connectToSubTransOutputFields( meta, subTransMeta, subTransNode, descriptor );

    node.setProperty( TRANSFORMATION_TO_EXECUTE, transformationPath );

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
    return KettleAnalyzerUtil.getSubTransMeta( filePath );
  }

  public void connectToSubTransOutputFields( TransExecutorMeta meta, TransMeta subTransMeta,
                                             IMetaverseNode subTransNode,
                                             IComponentDescriptor descriptor ) {

    if ( meta.getOutputRowsSourceStep() != null ) {
      String outputStep = meta.getOutputRowsSourceStep();

      for ( int i = 0; i < meta.getOutputRowsField().length; i++ ) {
        String fieldName = meta.getOutputRowsField()[ i ];
        IMetaverseNode outNode = getOutputs().findNode( outputStep, fieldName );
        if ( outNode != null ) {
          // add link, if needed, from sub tran result fields to the node just created
          subtransAnalyzer.linkResultFieldToSubTrans( outNode, subTransMeta, subTransNode, descriptor );
        }
      }
    }
    if ( meta.getExecutorsOutputStep() != null ) {
      String outputStep = meta.getExecutorsOutputStep();

      for ( int i = 0; i < meta.getOutputRowsField().length; i++ ) {
        String fieldName = meta.getOutputRowsField()[ i ];
        IMetaverseNode outNode = getOutputs().findNode( outputStep, fieldName );
        if ( outNode != null ) {
          // add link, if needed, from sub tran result fields to the node just created
          subtransAnalyzer.linkResultFieldToSubTrans( outNode, subTransMeta, subTransNode, descriptor );
        }
      }
    }
  }

  /**
   * Add links from all incoming stream fields to the fields in the sub transformation
   *
   * @param subTransMeta
   * @param subTransNode
   * @param descriptor
   */
  public void connectToSubTransInputFields( TransMeta subTransMeta,
                                            IMetaverseNode subTransNode,
                                            IComponentDescriptor descriptor ) {

    Set<StepField> incomingFields = getInputs().getFieldNames();
    for ( StepField field : incomingFields ) {
      IMetaverseNode inputNode = getInputs().findNode( field );
      subtransAnalyzer.linkUsedFieldToSubTrans( inputNode, subTransMeta, subTransNode, descriptor );
    }

  }

  @Override
  protected Map<String, RowMetaInterface> getOutputRowMetaInterfaces( TransExecutorMeta meta ) {
    Map<String, RowMetaInterface> outputFields = new HashMap<>();
    String[] nextStepNames = parentTransMeta.getNextStepNames( parentStepMeta );
    for ( String nextStepName : nextStepNames ) {
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
    HashSet<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<>();
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

  @Override
  public IClonableStepAnalyzer newInstance() {
    return new TransExecutorStepAnalyzer();
  }
}

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

package org.pentaho.metaverse.analyzer.kettle.step.jobexecutor;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.ProgressNullMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.jobexecutor.JobExecutorMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.JobAnalyzer;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryAnalyzerProvider;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.pentaho.metaverse.api.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class provides an analyzer for the "Execute Job" step
 */
public class JobExecutorStepAnalyzer extends StepAnalyzer<JobExecutorMeta> {

  public static final String JOB_TO_EXECUTE = "jobToExecute";
  public static final String EXECUTION_RESULTS_TARGET = "executionResultsTarget";
  public static final String OUTPUT_ROWS_TARGET = "outputRowsTarget";
  public static final String RESULT_FILES_TARGET = "resultFilesTarget";
  private Logger log = LoggerFactory.getLogger( JobExecutorStepAnalyzer.class );


  @Override
  protected Set<StepField> getUsedFields( JobExecutorMeta meta ) {
    // add uses links to all incoming fields
    return getInputs().getFieldNames();
  }

  @Override
  protected boolean isPassthrough( StepField originalFieldName ) {
    // there are no passthrough fields
    return false;
  }

  @Override
  protected void customAnalyze( JobExecutorMeta meta, IMetaverseNode node )
    throws MetaverseAnalyzerException {

    String jobPath = meta.getFileName();
    JobMeta subJobMeta = null;
    Repository repo = parentTransMeta.getRepository();

    MetaverseAnalyzerException exception = null;
    switch ( meta.getSpecificationMethod() ) {
      case FILENAME:
        jobPath = parentTransMeta.environmentSubstitute( meta.getFileName() );
        try {
          String normalized = KettleAnalyzerUtil.normalizeFilePath( jobPath );

          subJobMeta = getSubJobMeta( parentTransMeta, normalized );
          jobPath = normalized;

        } catch ( Exception e ) {
          exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.SubJobNotFoundInParentTrans",
            jobPath, parentTransMeta.toString() ), e );
        }
        break;
      case REPOSITORY_BY_NAME:
        if ( repo != null ) {
          String dir = parentTransMeta.environmentSubstitute( meta.getDirectoryPath() );
          String file = parentTransMeta.environmentSubstitute( meta.getJobName() );
          try {
            RepositoryDirectoryInterface rdi = repo.findDirectory( dir );
            subJobMeta = repo.loadJob( file, rdi, null, null );
            String filename = subJobMeta.getFilename() == null ? subJobMeta.toString() : subJobMeta.getFilename();
            jobPath = filename + "." + subJobMeta.getDefaultExtension();
          } catch ( KettleException e ) {
            exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.SubJobNotFoundInParentTrans",
              file, parentTransMeta.toString() ), e );
          }
        } else {
          exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.MissingConnectionForTransSubJob",
            parentTransMeta.toString() ) );
        }
        break;
      case REPOSITORY_BY_REFERENCE:
        if ( repo != null ) {
          try {
            subJobMeta = repo.loadJob( meta.getJobObjectId(), null );
            String filename = subJobMeta.getFilename() == null ? subJobMeta.toString() : subJobMeta.getFilename();
            jobPath = filename + "." + subJobMeta.getDefaultExtension();
          } catch ( KettleException e ) {
            exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.SubJobNotFoundInParentTrans",
              ( meta.getJobObjectId() == null ? "N/A" : meta.getJobObjectId().toString() ),
              parentTransMeta.toString() ), e );
          }
        } else {
          exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.MissingConnectionForTransSubJob",
            parentTransMeta.toString() ) );
        }
        break;
    }
    rootNode.setProperty( DictionaryConst.PROPERTY_PATH, jobPath );

    if ( exception != null ) {
      throw exception;
    }

    subJobMeta.copyVariablesFrom( parentTransMeta );
    subJobMeta.setFilename( jobPath );

    // analyze the sub trans?

    IComponentDescriptor ds = new MetaverseComponentDescriptor(
      subJobMeta.getName(),
      DictionaryConst.NODE_TYPE_JOB,
      descriptor.getNamespace().getParentNamespace() );

    IMetaverseNode jobNode = createNodeFromDescriptor( ds );
    jobNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, ds.getNamespaceId() );
    jobNode.setProperty( DictionaryConst.PROPERTY_PATH, jobPath );
    jobNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

    metaverseBuilder.addLink( node, DictionaryConst.LINK_EXECUTES, jobNode );

    final IDocument subTransDocument = KettleAnalyzerUtil.buildDocument( getMetaverseBuilder(), subJobMeta,
      jobPath, getDocumentDescriptor().getNamespace() );
    node.setProperty( JOB_TO_EXECUTE, jobPath );

    if ( StringUtils.isNotEmpty( meta.getExecutionResultTargetStep() ) ) {
      node.setProperty( EXECUTION_RESULTS_TARGET, meta.getExecutionResultTargetStep() );
    }

    if ( StringUtils.isNotEmpty( meta.getResultFilesTargetStep() ) ) {
      node.setProperty( RESULT_FILES_TARGET, meta.getResultFilesTargetStep() );
    }

    // pull in the sub-job lineage only if the consolidateGraphs flag is set to true
    if ( MetaverseConfig.consolidateSubGraphs() ) {
      final IComponentDescriptor subtransDocumentDescriptor = new MetaverseComponentDescriptor(
        subTransDocument.getStringID(), DictionaryConst.NODE_TYPE_TRANS, getDocumentDescriptor().getNamespace(),
        getDescriptor().getContext() );

      // analyze the sub-job
      final JobAnalyzer jobAnalyzer = new JobAnalyzer();
      jobAnalyzer.setJobEntryAnalyzerProvider( PentahoSystem.get( IJobEntryAnalyzerProvider.class ) );
      jobAnalyzer.setMetaverseBuilder( getMetaverseBuilder() );
      jobAnalyzer.analyze( subtransDocumentDescriptor, subJobMeta, jobNode, jobPath );

      connectToSubJobOutputFields( meta, subJobMeta, jobNode, descriptor );
    }

  }

  protected JobMeta getSubJobMeta( VariableSpace variableSpace, String filePath )
    throws FileNotFoundException, KettleXMLException, KettleMissingPluginsException {
    return new JobMeta( variableSpace, filePath, null, null, null );
  }

  protected void connectToSubJobOutputFields( JobExecutorMeta meta, JobMeta subJobMeta,
                                              IMetaverseNode subTransNode,
                                              IComponentDescriptor descriptor ) {

    if ( meta.getResultRowsTargetStep() != null ) {
      String outputStep = meta.getResultRowsTargetStep();

      for ( int i = 0; i < meta.getResultRowsField().length; i++ ) {
        String fieldName = meta.getResultRowsField()[i];
        IMetaverseNode outNode = getOutputs().findNode( outputStep, fieldName );
        // TODO
        /* if ( outNode != null ) {
          // add link, if needed, from sub tran result fields to the node just created
           linkResultFieldToSubTrans( outNode, subJobMeta, subTransNode, descriptor );
        }*/
      }
    }
  }


  @Override
  protected Map<String, RowMetaInterface> getOutputRowMetaInterfaces( JobExecutorMeta meta ) {
    Map<String, RowMetaInterface> outputFields = new HashMap<>();
    String[] nextStepNames = parentTransMeta.getNextStepNames( parentStepMeta );
    for ( int i = 0; i < nextStepNames.length; i++ ) {
      String nextStepName = nextStepNames[i];
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
    supportedSteps.add( JobExecutorMeta.class );
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
    return new JobExecutorStepAnalyzer();
  }
}

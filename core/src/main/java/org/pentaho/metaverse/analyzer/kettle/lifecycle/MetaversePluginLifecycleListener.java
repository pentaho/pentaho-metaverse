/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.metaverse.analyzer.kettle.lifecycle;

import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.metaverse.analyzer.kettle.JobAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.TransformationAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.jobentry.JobEntryAnalyzerProvider;
import org.pentaho.metaverse.analyzer.kettle.jobentry.JobEntryExternalResourceConsumerProvider;
import org.pentaho.metaverse.analyzer.kettle.jobentry.job.JobJobEntryAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.jobentry.transjob.TransJobEntryAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.StepAnalyzerProvider;
import org.pentaho.metaverse.analyzer.kettle.step.StepExternalResourceConsumerProvider;
import org.pentaho.metaverse.analyzer.kettle.step.calculator.CalculatorStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.csvfileinput.CsvFileInputExternalResourceConsumer;
import org.pentaho.metaverse.analyzer.kettle.step.csvfileinput.CsvFileInputStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.fixedfileinput.FixedFileInputExternalResourceConsumer;
import org.pentaho.metaverse.analyzer.kettle.step.fixedfileinput.FixedFileInputStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.groupby.GroupByStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.httpclient.HTTPClientExternalResourceConsumer;
import org.pentaho.metaverse.analyzer.kettle.step.httpclient.HTTPClientStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.httppost.HTTPPostExternalResourceConsumer;
import org.pentaho.metaverse.analyzer.kettle.step.httppost.HTTPPostStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.jobexecutor.JobExecutorStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.mapping.MappingAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.mergejoin.MergeJoinStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.numberrange.NumberRangeStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.rowsfromresult.RowsFromResultStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.rowstoresult.RowsToResultStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.selectvalues.SelectValuesStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.simplemapping.SimpleMappingAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.singlethreader.SingleThreaderStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.splitfields.SplitFieldsStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.streamlookup.StreamLookupStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.stringoperations.StringOperationsStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.stringscut.StringsCutStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.stringsreplace.StringsReplaceStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.tableinput.TableInputExternalResourceConsumer;
import org.pentaho.metaverse.analyzer.kettle.step.tableinput.TableInputStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.tableoutput.TableOutputExternalResourceConsumer;
import org.pentaho.metaverse.analyzer.kettle.step.tableoutput.TableOutputStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.textfileinput.TextFileInputExternalResourceConsumer;
import org.pentaho.metaverse.analyzer.kettle.step.textfileinput.TextFileInputStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.textfileoutput.TextFileOutputExternalResourceConsumer;
import org.pentaho.metaverse.analyzer.kettle.step.textfileoutput.TextFileOutputStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.transexecutor.TransExecutorStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.valuemapper.ValueMapperStepAnalyzer;
import org.pentaho.metaverse.api.MetaverseObjectFactory;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.JobEntryDatabaseConnectionAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepDatabaseConnectionAnalyzer;
import org.pentaho.metaverse.client.LineageClient;
import org.pentaho.metaverse.graph.BlueprintsGraphMetaverseReader;
import org.pentaho.metaverse.impl.DocumentController;
import org.pentaho.metaverse.impl.MetaverseBuilder;
import org.pentaho.metaverse.impl.MetaverseDocumentLocatorProvider;
import org.pentaho.metaverse.impl.VfsLineageCollector;
import org.pentaho.platform.engine.core.system.PentahoSystem;

@KettleLifecyclePlugin( id = "MetaversePlugin", name = "MetaversePlugin" )
public class MetaversePluginLifecycleListener implements KettleLifecycleListener {
  @Override public void onEnvironmentInit() throws LifecycleException {
    // REGISTER OBJECT FACTORY
    PentahoSystem.registerObject( MetaverseObjectFactory.getInstance() );

    // DB CONNECTION ANALYZERS
    StepDatabaseConnectionAnalyzer stepDatabaseConnectionAnalyzer = new StepDatabaseConnectionAnalyzer();
    stepDatabaseConnectionAnalyzer.setMetaverseBuilder( MetaverseBuilder.getInstance() );
    PentahoSystem.registerObject( stepDatabaseConnectionAnalyzer );

    JobEntryDatabaseConnectionAnalyzer jobEntryDatabaseConnectionAnalyzer = new JobEntryDatabaseConnectionAnalyzer();
    jobEntryDatabaseConnectionAnalyzer.setMetaverseBuilder( MetaverseBuilder.getInstance() );
    PentahoSystem.registerObject( jobEntryDatabaseConnectionAnalyzer );

    // CREATE AND REGISTER STEP ANALYZERS
    PentahoSystem.registerObject( new SimpleMappingAnalyzer() );
    PentahoSystem.registerObject( new MappingAnalyzer() );

    TableOutputStepAnalyzer tableOutputStepAnalyzer = new TableOutputStepAnalyzer();
    tableOutputStepAnalyzer.setConnectionAnalyzer( stepDatabaseConnectionAnalyzer );
    tableOutputStepAnalyzer.setExternalResourceConsumer( TableOutputExternalResourceConsumer.getInstance() );
    PentahoSystem.registerObject( tableOutputStepAnalyzer );

    TableInputStepAnalyzer tableInputStepAnalyzer = new TableInputStepAnalyzer();
    tableInputStepAnalyzer.setConnectionAnalyzer( stepDatabaseConnectionAnalyzer );
    tableInputStepAnalyzer.setExternalResourceConsumer( TableInputExternalResourceConsumer.getInstance() );
    PentahoSystem.registerObject( tableInputStepAnalyzer );

    TextFileInputStepAnalyzer oldTextFileInputStepAnalyzer = new TextFileInputStepAnalyzer();
    oldTextFileInputStepAnalyzer.setExternalResourceConsumer( TextFileInputExternalResourceConsumer.getInstance() );
    PentahoSystem.registerObject( oldTextFileInputStepAnalyzer );

    org.pentaho.metaverse.analyzer.kettle.step.fileinput.text.TextFileInputStepAnalyzer textFileInputStepAnalyzer
      = new org.pentaho.metaverse.analyzer.kettle.step.fileinput.text.TextFileInputStepAnalyzer();
    textFileInputStepAnalyzer.setExternalResourceConsumer(
      org.pentaho.metaverse.analyzer.kettle.step.fileinput.text.TextFileInputExternalResourceConsumer.getInstance() );
    PentahoSystem.registerObject( textFileInputStepAnalyzer );

    PentahoSystem.registerObject( new SelectValuesStepAnalyzer() );
    PentahoSystem.registerObject( new NumberRangeStepAnalyzer() );
    PentahoSystem.registerObject( new ValueMapperStepAnalyzer() );

    TextFileOutputStepAnalyzer textFileOutputStepAnalyzer = new TextFileOutputStepAnalyzer();
    textFileOutputStepAnalyzer.setExternalResourceConsumer( TextFileOutputExternalResourceConsumer.getInstance() );
    PentahoSystem.registerObject( textFileOutputStepAnalyzer );

    PentahoSystem.registerObject( new MergeJoinStepAnalyzer() );
    PentahoSystem.registerObject( new StreamLookupStepAnalyzer() );
    PentahoSystem.registerObject( new CalculatorStepAnalyzer() );
    PentahoSystem.registerObject( new GroupByStepAnalyzer() );
    PentahoSystem.registerObject( new SplitFieldsStepAnalyzer() );
    PentahoSystem.registerObject( new TransExecutorStepAnalyzer() );
    PentahoSystem.registerObject( new JobExecutorStepAnalyzer() );
    PentahoSystem.registerObject( new RowsToResultStepAnalyzer() );
    PentahoSystem.registerObject( new RowsFromResultStepAnalyzer() );
    PentahoSystem.registerObject( new StringOperationsStepAnalyzer() );
    PentahoSystem.registerObject( new StringsCutStepAnalyzer() );

    CsvFileInputStepAnalyzer csvFileInputStepAnalyzer = new CsvFileInputStepAnalyzer();
    csvFileInputStepAnalyzer.setExternalResourceConsumer( CsvFileInputExternalResourceConsumer.getInstance() );
    PentahoSystem.registerObject( csvFileInputStepAnalyzer );

    PentahoSystem.registerObject( new StringsReplaceStepAnalyzer() );

    FixedFileInputStepAnalyzer fixedFileInputStepAnalyzer = new FixedFileInputStepAnalyzer();
    fixedFileInputStepAnalyzer.setExternalResourceConsumer( FixedFileInputExternalResourceConsumer.getInstance() );
    PentahoSystem.registerObject( fixedFileInputStepAnalyzer );

    HTTPClientStepAnalyzer httpClientStepAnalyzer = new HTTPClientStepAnalyzer();
    httpClientStepAnalyzer.setExternalResourceConsumer( HTTPClientExternalResourceConsumer.getInstance() );
    PentahoSystem.registerObject( httpClientStepAnalyzer );

    HTTPPostStepAnalyzer httpPostStepAnalyzer = new HTTPPostStepAnalyzer();
    httpPostStepAnalyzer.setExternalResourceConsumer( HTTPPostExternalResourceConsumer.getInstance() );
    PentahoSystem.registerObject( httpPostStepAnalyzer );

    PentahoSystem.registerObject( new SingleThreaderStepAnalyzer() );

    //CREATE AND REGISTER JOB ENTRY ANALYZERS
    PentahoSystem.registerObject( new TransJobEntryAnalyzer() );
    PentahoSystem.registerObject( new JobJobEntryAnalyzer() );

    //CREATE AND REGISTER PROVIDERS
    // Providers will call PentahoSystem to get the analyzers they provide upon first invocation
    // We can't assume other plugins registering step analyzers have initialized before this one
    PentahoSystem.registerObject( StepAnalyzerProvider.getInstance() );
    PentahoSystem.registerObject( JobEntryAnalyzerProvider.getInstance() );

    PentahoSystem.registerObject( StepExternalResourceConsumerProvider.getInstance() );
    PentahoSystem.registerObject( JobEntryExternalResourceConsumerProvider.getInstance() );

    // DOCUMENT CONTROLLER
    DocumentController documentController = DocumentController.getInstance();
    documentController.setMetaverseObjectFactory( MetaverseObjectFactory.getInstance() );
    documentController.setMetaverseBuilder( MetaverseBuilder.getInstance() );
    TransformationAnalyzer transformationAnalyzer = new TransformationAnalyzer();
    documentController.addClonableAnalyzer( transformationAnalyzer );
    JobAnalyzer jobAnalyzer = new JobAnalyzer();
    documentController.addClonableAnalyzer( jobAnalyzer );
    PentahoSystem.registerObject( documentController );

    // METAVERSE READER
    PentahoSystem.registerObject( BlueprintsGraphMetaverseReader.getInstance() );
    PentahoSystem.registerObject( MetaverseDocumentLocatorProvider.getInstance() );
    PentahoSystem.registerObject( new VfsLineageCollector() );
    PentahoSystem.registerObject( new LineageClient() );
  }

  @Override public void onEnvironmentShutdown() {

  }
}

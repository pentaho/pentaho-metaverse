/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.concatfields.ConcatFieldsMeta;
import org.pentaho.metaverse.IntegrationTestUtil;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobRuntimeExtensionPoint;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.entry.JobEntryExternalResourceConsumerListener;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransformationRuntimeExtensionPoint;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.step.StepExternalResourceConsumerListener;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by mburgess on 12/9/14.
 */
@RunWith( Parameterized.class )
public class ExternalResourceConsumerIT {

  private static final String REPO_PATH = "src/it/resources/repo";

  private String transOrJobPath;
  private Map<String, String> variables;

  @Parameterized.Parameters( name = "{0}" )
  public static Collection props() {
    Object[][] inputs = new Object[][]{
      {
        REPO_PATH + "/Textfile input - fixed length.ktr",
        new HashMap<String, String>() {{
          put( "Internal.Transformation.Filename.Directory", REPO_PATH );
        }}
      },
      {
        REPO_PATH + "/CSV input.ktr",
        new HashMap<String, String>() {{
          put( "Internal.Transformation.Filename.Directory", REPO_PATH );
        }}
      },
      {
        REPO_PATH + "/Textfile input - filename from field.ktr",
        new HashMap<String, String>() {{
          put( "Internal.Transformation.Filename.Directory", REPO_PATH );
        }}
      },
      {
        REPO_PATH + "/process all tables/Process all tables.kjb",
        new HashMap<String, String>() {{
          put( "Internal.Transformation.Filename.Directory", REPO_PATH + "/process all tables" );
          put( "Internal.Job.Filename.Directory", REPO_PATH + "/process all tables" );
        }}
      }
    };
    return Arrays.asList( inputs );
  }

  public ExternalResourceConsumerIT( String transOrJobPath, Map<String, String> variables ) {
    this.transOrJobPath = transOrJobPath;
    this.variables = variables;
  }

  @BeforeClass
  public static void init() throws Exception {

    IntegrationTestUtil.initializePentahoSystem( "src/it/resources/solution/system/pentahoObjects.spring.xml" );

    ExtensionPointPluginType.getInstance().registerCustom( TransformationRuntimeExtensionPoint.class,
      "custom", "transExecutionProfile", "TransformationStartThreads", "no description", null );
    ExtensionPointPluginType.getInstance().registerCustom( JobRuntimeExtensionPoint.class,
      "custom", "jobRuntimeMetaverse", "JobStart", "no description", null );

    ExtensionPointPluginType.getInstance().registerCustom(
      StepExternalResourceConsumerListener.class,
      "custom", "stepExternalResource", "StepBeforeStart", "no description", null );

    ExtensionPointPluginType.getInstance().registerCustom(
      JobEntryExternalResourceConsumerListener.class,
      "custom", "jobEntryExternalResource", "JobBeforeJobEntryExecution", "no description", null );


    KettleEnvironment.init( false );
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );

    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    StepPluginType.getInstance().handlePluginAnnotation(
            ConcatFieldsMeta.class,
            ConcatFieldsMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
            Collections.emptyList(), false, null );
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    IntegrationTestUtil.shutdownPentahoSystem();
  }

  @Test
  public void testExternalResourceConsumer() throws Exception {
    FileInputStream xmlStream = new FileInputStream( transOrJobPath );
    Variables vars = new Variables();

    for ( String key : variables.keySet() ) {
      vars.setVariable( key, variables.get( key ) );
    }

    // run the trans or job
    if ( transOrJobPath.endsWith( ".ktr" ) ) {
      KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
      TransMeta tm = new TransMeta( xmlStream, null, true, vars, null );
      tm.setFilename( tm.getName() );
      Trans trans = new Trans( tm, null, tm.getName(), REPO_PATH, transOrJobPath );
      for ( String var : vars.listVariables() ) {
        trans.setVariable( var, vars.getVariable( var ) );
      }

      trans.execute( null );
      trans.waitUntilFinished();

      assertEquals("Found errors", 0, trans.getResult().getNrErrors());
    } else {
      KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.KITCHEN );
      JobMeta jm = new JobMeta( new Variables(), transOrJobPath, null, null, null );
      jm.setFilename( jm.getName() );
      Job job = new Job( null, jm );
      Variables variables = new Variables();
      variables.initializeVariablesFrom( job.getParentJob() );
      jm.setInternalKettleVariables( variables );
      for ( String var : vars.listVariables() ) {
        jm.setVariable( var, vars.getVariable( var ) );
      }
      job.copyParametersFrom( jm );
      job.copyVariablesFrom( jm );
      job.activateParameters();

      // We have to call the extension point ourselves -- don't ask :(
      ExtensionPointHandler.callExtensionPoint( job.getLogChannel(), KettleExtensionPoint.JobStart.id, job );
      Result result = job.execute( 0, null );
      job.fireJobFinishListeners();
      assertEquals("Found errors", 0, result.getNrErrors());
    }
  }
}

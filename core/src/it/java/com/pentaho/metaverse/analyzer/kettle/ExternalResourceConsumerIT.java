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
 */

package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.metaverse.IntegrationTestUtil;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobRuntimeExtensionPoint;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.job.entry.JobEntryExternalResourceConsumerListener;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.step.StepExternalResourceConsumerListener;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransformationRuntimeExtensionPoint;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        REPO_PATH + "/validation/xml_output.ktr",
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
      job.execute( 0, null );
      job.fireJobFinishListeners();
    }
  }
}

/*
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

package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.metaverse.analyzer.kettle.extensionpoints.ExternalResourceConsumerMap;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.TransformationRuntimeExtensionPoint;
import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumerPluginRegistrar;
import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumerPluginType;
import com.pentaho.metaverse.analyzer.kettle.step.TextFileInputStepAnalyzer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.variables.Variables;
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

  private TransMeta tm;

  private String ktrPath;
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
        REPO_PATH + "/Textfile input - filename from field.ktr",
        new HashMap<String, String>() {{
          put( "Internal.Transformation.Filename.Directory", REPO_PATH );
        }}
      }
    };
    return Arrays.asList( inputs );
  }

  public ExternalResourceConsumerIT( String ktrPath, Map<String, String> variables ) {
    this.ktrPath = ktrPath;
    this.variables = variables;
  }

  @BeforeClass
  public static void init() throws Exception {
    PluginRegistry registry = PluginRegistry.getInstance();
    ExternalResourceConsumerMap.ExternalResourceConsumerMapBuilder builder = new
      ExternalResourceConsumerMap.ExternalResourceConsumerMapBuilder();

    ExternalResourceConsumerPluginRegistrar registrar = new ExternalResourceConsumerPluginRegistrar();
    registrar.init( registry );

    ExternalResourceConsumerPluginType.getInstance().registerCustom(
      TextFileInputStepAnalyzer.TextFileInputExternalResourceConsumer.class,
      "internal",
      "TextFileInputExternalResourceConsumer",
      "TextFileInputExternalResourceConsumer",
      "TextFileInputExternalResourceConsumer",
      null
    );

    ExtensionPointPluginType.getInstance().registerCustom( TransformationRuntimeExtensionPoint.class,
      "custom", "transExecutionProfile", "TransformationStartThreads", "no description", null );

    ExtensionPointPluginType.getInstance().registerCustom(
      TransformationRuntimeExtensionPoint.ExternalResourceConsumerListener.class,
      "custom", "stepExternalResource", "StepBeforeStart", "no description", null );


    KettleEnvironment.init();
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );

    builder.onEnvironmentInit();
  }

  @Test
  public void testExternalResourceConsumer() throws Exception {
    FileInputStream xmlStream = new FileInputStream( ktrPath );
    Variables vars = new Variables();

    for ( String key : variables.keySet() ) {
      vars.setVariable( key, variables.get( key ) );
    }

    // run the trans
    tm = new TransMeta( xmlStream, null, true, vars, null );
    tm.setFilename( tm.getName() );
    Trans trans = new Trans( tm, null, tm.getName(), REPO_PATH, ktrPath );
    for ( String var : vars.listVariables() ) {
      trans.setVariable( var, vars.getVariable( var ) );
    }

    trans.execute( null );
    trans.waitUntilFinished();
  }
}

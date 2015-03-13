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
package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumerPluginRegistrar;
import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumerPluginType;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetaverseKettleLifecycleHandlerTest {

  @Test
  public void testPluginAdded() throws Exception {
    PluginRegistry registry = PluginRegistry.getInstance();
    MetaverseKettleLifecycleHandler metaverseKettleLifecycleHandler = new MetaverseKettleLifecycleHandler();

    ExternalResourceConsumerPluginRegistrar registrar = new ExternalResourceConsumerPluginRegistrar();
    registrar.init( registry );
    PluginRegistry.init();

    // Call once to add plugin listener
    metaverseKettleLifecycleHandler.onEnvironmentInit();

    // Register a fake step plugin
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, createMockStepPlugin() );

    // Register a fake job entry plugin
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, createMockJobEntryPlugin() );

    // No-op, called for coverage
    metaverseKettleLifecycleHandler.onEnvironmentShutdown();
  }

  @Test
  public void testPluginRemoved() throws Exception {
    PluginRegistry registry = PluginRegistry.getInstance();
    MetaverseKettleLifecycleHandler metaverseKettleLifecycleHandler = new MetaverseKettleLifecycleHandler();

    ExternalResourceConsumerPluginRegistrar registrar = new ExternalResourceConsumerPluginRegistrar();
    registrar.init( registry );
    PluginRegistry.init();

    // Call once to add plugin listener
    metaverseKettleLifecycleHandler.onEnvironmentInit();

    // Register a fake step plugin
    PluginInterface mockStepPlugin = createMockStepPlugin();
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, mockStepPlugin );

    // Register a fake job entry plugin
    PluginInterface mockJobEntryPlugin = createMockJobEntryPlugin();
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, mockJobEntryPlugin );

    registry.removePlugin( ExternalResourceConsumerPluginType.class, mockStepPlugin );
    registry.removePlugin( ExternalResourceConsumerPluginType.class, mockJobEntryPlugin );

  }

  @Test
  public void testPluginChanged() throws Exception {
    PluginRegistry registry = PluginRegistry.getInstance();
    MetaverseKettleLifecycleHandler metaverseKettleLifecycleHandler = new MetaverseKettleLifecycleHandler();

    ExternalResourceConsumerPluginRegistrar registrar = new ExternalResourceConsumerPluginRegistrar();
    registrar.init( registry );
    PluginRegistry.init();

    // Call once to add plugin listener
    metaverseKettleLifecycleHandler.onEnvironmentInit();

    // Register a fake step plugin
    PluginInterface mockStepPlugin = createMockStepPlugin();
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, mockStepPlugin );

    // Register a fake job entry plugin
    PluginInterface mockJobEntryPlugin = createMockJobEntryPlugin();
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, mockJobEntryPlugin );

    // Re-register new versions of the plugin to get pluginChanged() invoked
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, mockStepPlugin );
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, mockJobEntryPlugin );

  }

  private PluginInterface createMockStepPlugin() {
    // Create a fake step consumer plugin to exercise the map building logic
    PluginInterface mockStepPlugin = mock( PluginInterface.class );
    when( mockStepPlugin.getIds() ).thenReturn( new String[]{ "testStepId" } );
    when( mockStepPlugin.getName() ).thenReturn( "testStepName" );
    IStepExternalResourceConsumer stepConsumer = new StepExternalResourceConsumerStub();
    Map<Class<?>, String> stepClassMap = new HashMap<Class<?>, String>();
    stepClassMap.put( IExternalResourceConsumer.class, stepConsumer.getClass().getName() );
    doReturn( IExternalResourceConsumer.class ).when( mockStepPlugin ).getMainType();
    // Then add a class to the maps to get through plugin registration
    when( mockStepPlugin.getClassMap() ).thenReturn( stepClassMap );
    return mockStepPlugin;
  }

  private PluginInterface createMockJobEntryPlugin() {
    // Create a fake job entry consumer plugin to exercise the map building logic
    PluginInterface mockJobEntryPlugin = mock( PluginInterface.class );
    when( mockJobEntryPlugin.getIds() ).thenReturn( new String[]{ "testJobEntryId" } );
    when( mockJobEntryPlugin.getName() ).thenReturn( "testJobEntryName" );
    IJobEntryExternalResourceConsumer jobEntryConsumer = new JobEntryExternalResourceConsumerStub();
    Map<Class<?>, String> jobEntryClassMap = new HashMap<Class<?>, String>();
    jobEntryClassMap.put( IExternalResourceConsumer.class, jobEntryConsumer.getClass().getName() );
    doReturn( IExternalResourceConsumer.class ).when( mockJobEntryPlugin ).getMainType();
    when( mockJobEntryPlugin.getClassMap() ).thenReturn( jobEntryClassMap );
    return mockJobEntryPlugin;
  }
}

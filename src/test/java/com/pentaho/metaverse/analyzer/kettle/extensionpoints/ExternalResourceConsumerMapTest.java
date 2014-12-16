package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumerPluginRegistrar;
import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumerPluginType;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class ExternalResourceConsumerMapTest {

  ExternalResourceConsumerMap map;

  @Before
  public void setUp() throws Exception {
    map = ExternalResourceConsumerMap.getInstance();
  }

  @Test
  public void testGetInstance() throws Exception {
    assertNotNull( map );
  }

  @Test
  public void testGetStepExternalResourceConsumers() throws Exception {
    assertNotNull( map.getStepExternalResourceConsumers( BaseStepMeta.class ) );
  }

  @Test
  public void testGetJobEntryExternalResourceConsumers() throws Exception {
    assertNotNull( map.getJobEntryExternalResourceConsumers( JobEntryBase.class ) );
  }

  @Test
  public void testExternalResourceConsumerMapBuilder() throws Exception {
    PluginRegistry registry = PluginRegistry.getInstance();
    ExternalResourceConsumerMap.ExternalResourceConsumerMapBuilder builder = new
      ExternalResourceConsumerMap.ExternalResourceConsumerMapBuilder();

    ExternalResourceConsumerPluginRegistrar registrar = new ExternalResourceConsumerPluginRegistrar();
    registrar.init( registry );
    PluginRegistry.init();

    // Create a fake step consumer plugin to exercise the map building logic
    PluginInterface mockStepPlugin = mock( PluginInterface.class );
    when( mockStepPlugin.getIds() ).thenReturn( new String[]{ "testStepId" } );
    when( mockStepPlugin.getName() ).thenReturn( "testStepName" );
    IStepExternalResourceConsumer stepConsumer = new StepExternalResourceConsumerStub();
    Map<Class<?>, String> stepClassMap = new HashMap<Class<?>, String>();
    stepClassMap.put( IExternalResourceConsumer.class, stepConsumer.getClass().getName() );
    doReturn( IExternalResourceConsumer.class ).when( mockStepPlugin ).getMainType();
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, mockStepPlugin );

    // Create a fake step consumer plugin to exercise the map building logic
    PluginInterface mockJobEntryPlugin = mock( PluginInterface.class );
    when( mockJobEntryPlugin.getIds() ).thenReturn( new String[]{ "testJobEntryId" } );
    when( mockJobEntryPlugin.getName() ).thenReturn( "testJobEntryName" );
    IJobEntryExternalResourceConsumer jobEntryConsumer = new JobEntryExternalResourceConsumerStub();
    Map<Class<?>, String> jobEntryClassMap = new HashMap<Class<?>, String>();
    jobEntryClassMap.put( IExternalResourceConsumer.class, jobEntryConsumer.getClass().getName() );
    doReturn( IExternalResourceConsumer.class ).when( mockJobEntryPlugin ).getMainType();
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, mockJobEntryPlugin );

    // Call once to generate plugin exception
    try {
      builder.onEnvironmentInit();
      fail( "Should have generated a LifecycleException from a KettlePluginException" );
    } catch ( LifecycleException le ) {
      // We're good, we expected this
    }

    // Then add a class to the maps to get through plugin registration
    when( mockStepPlugin.getClassMap() ).thenReturn( stepClassMap );
    when( mockJobEntryPlugin.getClassMap() ).thenReturn( jobEntryClassMap );

    builder.onEnvironmentInit();

    // No-op, called for coverage
    builder.onEnvironmentShutdown();
  }

  public static class StepExternalResourceConsumerStub implements IStepExternalResourceConsumer {

    @Override
    public boolean isDataDriven( Object meta ) {
      return false;
    }

    @Override
    public Collection<IExternalResourceInfo> getResourcesFromMeta( Object meta ) {
      return null;
    }

    @Override
    public Collection<IExternalResourceInfo> getResourcesFromRow(
      Object meta, RowMetaInterface rowMeta, Object[] row ) {
      return null;
    }

    @Override
    public Class<?> getMetaClass() {
      return BaseStepMeta.class;
    }
  }

  public static class JobEntryExternalResourceConsumerStub implements IJobEntryExternalResourceConsumer {

    @Override
    public boolean isDataDriven( Object meta ) {
      return false;
    }

    @Override
    public Collection<IExternalResourceInfo> getResourcesFromMeta( Object meta ) {
      return null;
    }

    @Override
    public Collection<IExternalResourceInfo> getResourcesFromRow(
      Object meta, RowMetaInterface rowMeta, Object[] row ) {
      return null;
    }

    @Override
    public Class<?> getMetaClass() {
      return JobEntryBase.class;
    }
  }
}

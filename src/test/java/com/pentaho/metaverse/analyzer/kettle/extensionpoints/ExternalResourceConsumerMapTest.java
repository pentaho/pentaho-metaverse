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
  public void testExternalResourceConsumerMapBuilder() throws Exception {
    PluginRegistry registry = PluginRegistry.getInstance();
    ExternalResourceConsumerMap.ExternalResourceConsumerMapBuilder builder = new
        ExternalResourceConsumerMap.ExternalResourceConsumerMapBuilder();

    ExternalResourceConsumerPluginRegistrar registrar = new ExternalResourceConsumerPluginRegistrar();
    registrar.init( registry );
    PluginRegistry.init();

    // Create a fake plugin to exercise the map building logic
    PluginInterface mockPlugin = mock( PluginInterface.class );
    when( mockPlugin.getIds() ).thenReturn( new String[]{ "testId" } );
    IStepExternalResourceConsumer consumer = new StepExternalResourceConsumerStub();

    Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
    classMap.put( IExternalResourceConsumer.class, consumer.getClass().getName() );

    doReturn( IExternalResourceConsumer.class ).when( mockPlugin ).getMainType();
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, mockPlugin );

    // Call once to generate plugin exception
    try {
      builder.onEnvironmentInit();
      fail( "Should have generated a LifecycleException from a KettlePluginException" );
    } catch ( LifecycleException le ) {
      // We're good, we expected this
    }

    // Then add a class to the map to get through plugin registration
    when( mockPlugin.getClassMap() ).thenReturn( classMap );

    builder.onEnvironmentInit();

    // Noop, called for coverage
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
    public Class<?> getStepMetaClass() {
      return BaseStepMeta.class;
    }
  }
}

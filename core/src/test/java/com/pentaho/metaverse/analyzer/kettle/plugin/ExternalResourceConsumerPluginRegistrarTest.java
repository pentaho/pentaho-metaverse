package com.pentaho.metaverse.analyzer.kettle.plugin;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class ExternalResourceConsumerPluginRegistrarTest {

  private ExternalResourceConsumerPluginRegistrar registrar;

  @Before
  public void setUp() throws Exception {
    registrar = new ExternalResourceConsumerPluginRegistrar();
  }

  @Test
  public void testGetPluginId() throws Exception {
    assertNull( registrar.getPluginId( mock( PluginTypeInterface.class ).getClass(), null ) );
  }

  @Test
  public void testInit() throws Exception {
    PluginRegistry pluginRegistry = PluginRegistry.getInstance();
    KettleClientEnvironment.init();
    registrar.init( pluginRegistry );
    List<PluginTypeInterface> pluginTypes = pluginRegistry.getAddedPluginTypes();
    assertNotNull( pluginTypes );
    assertTrue( pluginTypes.contains( ExternalResourceConsumerPluginType.getInstance() ) );

  }

  @Test
  public void testSearchForType() throws Exception {
    // Noop method, call for coverage
    registrar.searchForType( mock( PluginTypeInterface.class ) );
  }
}

package com.pentaho.metaverse.analyzer.kettle;

import org.junit.Test;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.INamespace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class BaseKettleMetaverseComponentTest {

  BaseKettleMetaverseComponent component = new BaseKettleMetaverseComponent() {
  };

  @Test
  public void testGetMetaverseBuilder() throws Exception {
    assertNull( component.getMetaverseBuilder() );
    component.metaverseBuilder = mock( IMetaverseBuilder.class );
    assertNotNull( component.getMetaverseBuilder() );
  }

  @Test
  public void testGetSiblingNamespace() throws Exception {
    INamespace namespace = mock( INamespace.class );

    // be sure we don't call through to the underlying namespace object if it is null
    assertNull( component.getSiblingNamespace( null, null, null ) );
    verify( namespace, times(0) ).getSiblingNamespace( anyString(), anyString() );

    // verify the delegate methods are called
    component.getSiblingNamespace( namespace, null, null );
    verify( namespace, times(1) ).getSiblingNamespace( null, null );

    component.getSiblingNamespace( namespace, "name", null );
    verify( namespace, times(1) ).getSiblingNamespace( "name", null );

    component.getSiblingNamespace( namespace, "name", "type" );
    verify( namespace, times(1) ).getSiblingNamespace( "name", "type" );

  }
}

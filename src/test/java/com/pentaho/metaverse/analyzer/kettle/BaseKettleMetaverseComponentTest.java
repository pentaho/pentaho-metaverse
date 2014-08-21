package com.pentaho.metaverse.analyzer.kettle;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.INamespace;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    when( namespace.getChildNamespace( anyString(), anyString() ) ).thenReturn( namespace );
    when( namespace.getParentNamespace() ).thenReturn( null );
    assertNull( component.getSiblingNamespace( null, null, null ) );
    assertNull( component.getSiblingNamespace( namespace, null, null ) );
    assertNull( component.getSiblingNamespace( namespace, "name", null ) );
    assertNull( component.getSiblingNamespace( namespace, "name", "type" ) );
    when( namespace.getParentNamespace() ).thenReturn( namespace );
    assertNotNull( component.getSiblingNamespace( namespace, "name", null ) );
    assertNotNull( component.getSiblingNamespace( namespace, "name", "type" ) );
  }
}

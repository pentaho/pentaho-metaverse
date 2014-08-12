package com.pentaho.metaverse.impl;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.metaverse.INamespace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by gmoran on 8/7/14.
 */
public class MetaverseNamespaceTest {

  MetaverseNamespace namespace;

  @Before
  public void before( ) {

    INamespace root = new MetaverseNamespace( null, "level 1", "type", null );
    INamespace level2 = new MetaverseNamespace( root, "level 2", "type", null );
    INamespace level3 = new MetaverseNamespace( level2, "level 3", "type", null );

    namespace = new MetaverseNamespace( level3, "level 4", "type", null );

  }

  @Test
  public void testGetNamespaceId( ) {
    assertEquals( "level 1~type~level 2~type~level 3~type~level 4~type", namespace.getNamespaceId() );
  }

  @Test
  public void testNamespaceNull( ) {
    INamespace ns = new MetaverseNamespace( null, null, null, null );
    assertNull( ns.getNamespaceId() );
  }

  @Test
  public void testParentNamespace( ) {
    assertEquals( "level 1~type", namespace.getParentNamespace().getParentNamespace().getParentNamespace().getNamespaceId() );
  }

}

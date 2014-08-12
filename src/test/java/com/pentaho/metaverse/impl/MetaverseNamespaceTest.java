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

    INamespace root = new MetaverseNamespace( null, "level 1", null );
    INamespace level2 = new MetaverseNamespace( root, "level 2", null );
    INamespace level3 = new MetaverseNamespace( level2, "level 3", null );

    namespace = new MetaverseNamespace( level3, "level 4", null );

  }

  @Test
  public void testGetNamespaceId( ) {
    assertEquals( "level 1~level 2~level 3~level 4", namespace.getNamespaceId() );
  }

  @Test
  public void testNamespaceNull( ) {
    INamespace ns = new MetaverseNamespace( null, null, null );
    assertNull( ns.getNamespaceId() );
  }

  @Test
  public void testParentNamespace( ) {
    assertEquals( "level 1", namespace.getParentNamespace().getParentNamespace().getParentNamespace().getNamespaceId() );
  }

}

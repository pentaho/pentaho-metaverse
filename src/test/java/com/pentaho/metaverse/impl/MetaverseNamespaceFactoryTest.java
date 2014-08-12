package com.pentaho.metaverse.impl;

import com.pentaho.metaverse.api.INamespaceFactory;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.metaverse.INamespace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by gmoran on 8/7/14.
 */
public class MetaverseNamespaceFactoryTest {

  INamespaceFactory factory;

  INamespace namespace;

  @Before
  public void before( ) {

    INamespace root = new MetaverseNamespace( null, "level 1", "type", null );
    INamespace level2 = new MetaverseNamespace( root, "level 2", "type", null );
    INamespace level3 = new MetaverseNamespace( level2, "level 3", "type", null );

    namespace = new MetaverseNamespace( level3, "level 4", "type", null );

    factory = new NamespaceFactory();
  }

  @Test
  public void testCreateNamespace( ) {
    INamespace ns = factory.createNameSpace( namespace, "level 5", "type" );
    assertEquals( "level 1~type~level 2~type~level 3~type~level 4~type~level 5~type", ns.getNamespaceId() );
  }

  @Test
  public void testNamespaceNull( ) {
    INamespace ns = factory.createNameSpace( null, null, null );
    assertNull( ns.getNamespaceId() );
  }

}

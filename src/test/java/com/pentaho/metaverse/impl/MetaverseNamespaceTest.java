package com.pentaho.metaverse.impl;

import com.pentaho.metaverse.api.INamespaceFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.metaverse.INamespace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by gmoran on 8/7/14.
 */
@RunWith( MockitoJUnitRunner.class )
public class MetaverseNamespaceTest {

  MetaverseNamespace namespace;

  @Mock
  private INamespaceFactory mockFactory;

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

  @Test
  public void testGetChildNamespace() throws Exception {
    namespace = new MetaverseNamespace( null, "level 1", "type", mockFactory );
    when( mockFactory.createNameSpace( eq( namespace ), anyString(), anyString() ) ).thenReturn( null );

    namespace.getChildNamespace( "level 2", "type" );

    verify( mockFactory ).createNameSpace( eq( namespace ), anyString(), anyString() );
  }

  @Test
  public void testGetNamespaceId_multipleCalls() throws Exception {
    assertEquals( "level 1~type~level 2~type~level 3~type~level 4~type", namespace.getNamespaceId() );
    assertEquals( "level 1~type~level 2~type~level 3~type~level 4~type", namespace.getNamespaceId() );
    assertEquals( "level 1~type~level 2~type~level 3~type~level 4~type", namespace.getNamespaceId() );
  }

  @Test
  public void testConstructor() throws Exception {
    namespace = new MetaverseNamespace( null, "type", "level 1" );
  }
}

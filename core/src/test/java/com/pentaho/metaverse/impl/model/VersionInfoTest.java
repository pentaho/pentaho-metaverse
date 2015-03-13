package com.pentaho.metaverse.impl.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class VersionInfoTest {

  VersionInfo versionInfo;

  @Before
  public void setUp() throws Exception {
    versionInfo = new VersionInfo();
  }

  @Test
  public void testGetSetVersion() throws Exception {
    assertNull( versionInfo.getVersion() );
    versionInfo.setVersion( "test" );
    assertEquals( "test", versionInfo.getVersion() );
  }

  @Test
  public void testGetSetName() throws Exception {
    assertNull( versionInfo.getName() );
    versionInfo.setName( "test" );
    assertEquals( "test", versionInfo.getName() );
  }

  @Test
  public void testGetSetDescription() throws Exception {
    assertNull( versionInfo.getDescription() );
    versionInfo.setDescription( "test" );
    assertEquals( "test", versionInfo.getDescription() );
  }
}

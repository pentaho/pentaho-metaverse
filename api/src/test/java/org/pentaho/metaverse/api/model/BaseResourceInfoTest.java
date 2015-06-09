package org.pentaho.metaverse.api.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BaseResourceInfoTest {

  BaseResourceInfo resourceInfo;

  @Before
  public void setUp() throws Exception {
    resourceInfo = new BaseResourceInfo();
  }

  @Test
  public void testGetSetType() throws Exception {
    assertNull( resourceInfo.getType() );
    resourceInfo.setType( "testType" );
    assertEquals( "testType", resourceInfo.getType() );

  }

  @Test
  public void testIsInputOutput() throws Exception {
    assertFalse( resourceInfo.isInput() );
    assertTrue( resourceInfo.isOutput() );
    resourceInfo.setInput( true );
    assertTrue( resourceInfo.isInput() );
    assertFalse( resourceInfo.isOutput() );
  }

  @Test
  public void testGetSetAttributes() throws Exception {
    assertTrue( resourceInfo.getAttributes().isEmpty() );
    resourceInfo.putAttribute( "testKey", "testValue" );
    assertEquals( "testValue", resourceInfo.getAttributes().get( "testKey" ) );
  }
}

package com.pentaho.metaverse.impl.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BaseInfoTest {

  BaseInfo baseInfo;

  @Before
  public void setUp() throws Exception {
    baseInfo = new BaseInfo();
  }

  @Test
  public void testGetSetName() throws Exception {
    assertNull( baseInfo.getName() );
    baseInfo.setName( "test" );
    assertEquals( "test", baseInfo.getName() );
  }

  @Test
  public void testGetSetDescription() throws Exception {
    assertNull( baseInfo.getDescription() );
    baseInfo.setDescription( "test" );
    assertEquals( "test", baseInfo.getDescription() );
  }
}

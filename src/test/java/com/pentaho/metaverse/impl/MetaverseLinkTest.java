/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */
package com.pentaho.metaverse.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class MetaverseLinkTest {

  @Mock
  IMetaverseNode fromNode;
  @Mock
  IMetaverseNode toNode;

  private MetaverseLink link;
  private MetaverseLink emptyLink;

  @Before
  public void before() {
    link = new MetaverseLink( fromNode, "uses", toNode );
    emptyLink = new MetaverseLink();
  }

  @Test
  public void testGetLabel() {
    assertEquals( "uses", link.getLabel() );
    assertNull( emptyLink.getLabel() );
  }

  @Test
  public void testGetNodes() {
    assertEquals( fromNode, link.getFromNode() );
    assertEquals( toNode, link.getToNode() );
  }

  @Test
  public void getAndSetProperty() {
    assertNull( link.getProperty( "TEST" ) );
    link.setProperty( "TEST", "value" );
    assertEquals( "value", link.getProperty( "TEST" ) );
  }

  @Test
  public void getPropertyKeys() {
    assertEquals( 0, emptyLink.getPropertyKeys().size() );

    // label is a property, so it should be the initial prop in our test link
    assertEquals( 1, link.getPropertyKeys().size() );

    link.setProperty( "TEST", "value" );
    link.setProperty( "alias", "val" );
    assertEquals( 3, link.getPropertyKeys().size() );
  }

  @Test
  public void removeProperty() {
    link.setProperty( "TEST", "value" );
    link.setProperty( "alias", "val" );

    assertNull( link.removeProperty( "does not exist in props" ) );
    assertEquals( "value", link.removeProperty( "TEST" ) );
    assertNull( link.getProperty( "TEST" ) );
  }


}

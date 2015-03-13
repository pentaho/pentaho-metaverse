/*
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

package com.pentaho.metaverse.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * User: RFellows Date: 10/13/14
 */
public class NamespaceTest {

  @Test
  public void testGetParentNamespace() throws Exception {
    Namespace ns = new Namespace( "{\"namespace\":{\"name\":\"FILE_SYSTEM_REPO\",\"type\":\"Locator\"}," +
      "\"path\":\"repo/Table Output - DataGrid to H2.ktr\",\"type\":\"Transformation\"}" );

    assertEquals( "{\"name\":\"FILE_SYSTEM_REPO\",\"type\":\"Locator\"}", ns.getParentNamespace().getNamespaceId() );
  }

  @Test
  public void testGetParentNamespace_nullNamespace() throws Exception {
    Namespace ns = new Namespace( null );
    assertNull( ns.getParentNamespace() );
  }

  @Test
  public void testGetParentNamespace_invalidJSON() throws Exception {
    Namespace ns = new Namespace( "{namespace\"={\"name\":\"FILE_SYSTEM_REPO\",\"type\":\"Locator\"}," +
      "\"path\":\"repo/Table Output - DataGrid to H2.ktr\",\"type\":\"Transformation\"}" );

    assertNull( ns.getParentNamespace() );
  }

  @Test
  public void testGetSiblingNamespace() throws Exception {
    Namespace ns = new Namespace( "{\"namespace\":{\"name\":\"FILE_SYSTEM_REPO\",\"type\":\"Locator\"}," +
      "\"name\":\"TEST\",\"type\":\"DUMMY\"}" );

    assertEquals( "{\"namespace\":{\"name\":\"FILE_SYSTEM_REPO\",\"type\":\"Locator\"}," +
        "\"name\":\"brother\",\"type\":\"newType\"}",
      ns.getSiblingNamespace( "brother", "newType" ).getNamespaceId() );

  }

  @Test
  public void testGetSiblingNamespace_nullNamespace() throws Exception {
    Namespace ns = new Namespace( null );

    assertNull( ns.getSiblingNamespace( "any", "any" ) );

  }

  @Test
  public void testGetSiblingNamespace_invalidJSON() throws Exception {
    Namespace ns = new Namespace( "{namespace\"={\"name\":\"FILE_SYSTEM_REPO\",\"type\":\"Locator\"}," +
      "\"name\":\"TEST\",\"type\":\"DUMMY\"}" );

    assertNull( ns.getSiblingNamespace( "any", "any" ) );

  }
}

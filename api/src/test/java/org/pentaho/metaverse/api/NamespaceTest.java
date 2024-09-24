/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.metaverse.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * User: RFellows Date: 10/13/14
 */
public class NamespaceTest {

  @Test
  public void testGetParentNamespace_objectParent() throws Exception {
    Namespace ns = new Namespace( "{\"namespace\":{\"name\":\"FILE_SYSTEM_REPO\",\"type\":\"Locator\"}," +
      "\"path\":\"repo/Table Output - DataGrid to H2.ktr\",\"type\":\"Transformation\"}" );

    assertEquals( "{\"name\":\"FILE_SYSTEM_REPO\",\"type\":\"Locator\"}", ns.getParentNamespace().getNamespaceId() );
  }

  @Test
  public void testGetParentNamespace_StringParent() throws Exception {
    Namespace ns = new Namespace( "{\"namespace\":\"PDI Engine\"," +
      "\"path\":\"repo/Table Output - DataGrid to H2.ktr\",\"type\":\"Transformation\"}" );

    assertEquals( "PDI Engine", ns.getParentNamespace().getNamespaceId() );
  }

  @Test
  public void testGetParentNamespace_HasBackslash() throws Exception {
    Namespace ns = new Namespace( "{\"namespace\":\"PDI Engine\"," +
      "\"path\":\"C:\\\\repo\\\\Table Output - DataGrid to H2.ktr\",\"type\":\"Transformation\"}" );

    assertEquals( "PDI Engine", ns.getParentNamespace().getNamespaceId() );
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

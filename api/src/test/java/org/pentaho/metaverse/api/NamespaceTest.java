/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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

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


package org.pentaho.dictionary;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.metaverse.api.IMetaverseNode;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class DictionaryHelperTest {

  protected static Set saveStateEntityTypes;

  /**
   * Current tests rely on state of {@link DictionaryHelper#ENTITY_NODE_TYPES}.
   * External tests modify the statically accessible state of the member variable, the saving and restoring state
   * is to prevent side effects.
   */
  @BeforeClass
  public static void beforeAll() {
    saveStateEntityTypes = new HashSet<>(DictionaryHelper.ENTITY_NODE_TYPES);
    DictionaryHelper.ENTITY_NODE_TYPES.clear();
  }

  /**
   * Current tests rely on state of {@link DictionaryHelper#ENTITY_NODE_TYPES}.
   * External tests modify the statically accessible state of the member variable, the saving and restoring state
   * is to prevent side effects.
   */
  @AfterClass
  public static void afterAll() {
    DictionaryHelper.ENTITY_NODE_TYPES.clear();
    DictionaryHelper.ENTITY_NODE_TYPES.addAll( saveStateEntityTypes );
  }

  @Test( expected = UnsupportedOperationException.class )
  public void testEnsureNonPublicConstructor() {
    DictionaryHelper dc = new DictionaryHelper();
  }

  @Test
  public void testNodesAndLinks() {

    Properties props = new Properties();
    props.put( DictionaryConst.PROPERTY_AUTHOR, "fred" );
    props.put( DictionaryConst.PROPERTY_LAST_MODIFIED, "2014-07-10 17:34:45" );
    IMetaverseNode transNode = DictionaryHelper.createMetaverseNode(
      DictionaryConst.NODE_TYPE_TRANS + "~my transform.ktr",
      "my transform", DictionaryConst.NODE_TYPE_TRANS, props );

    IMetaverseNode stepNode = DictionaryHelper.addChildNode(
      DictionaryConst.NODE_TYPE_TRANS_STEP + "~my transform.ktr~Table Input",
      "Table Input", DictionaryConst.NODE_TYPE_TRANS_STEP, null, transNode, DictionaryConst.LINK_CONTAINS );

    MetaverseTransientNode node1 = (MetaverseTransientNode) transNode;
    MetaverseTransientNode node2 = (MetaverseTransientNode) stepNode;

    assertNotNull( "Links is null", node1.getLinks() );
    assertEquals( "Links count is wrong", 1, node1.getLinks().size() );
    assertEquals( "Links node is wrong", node1, node1.getLinks().get( 0 ).getFromNode() );
    assertEquals( "Links node is wrong", node2, node1.getLinks().get( 0 ).getToNode() );
    assertEquals( "Links type is wrong", DictionaryConst.LINK_CONTAINS, node1.getLinks().get( 0 ).getLabel() );
    assertEquals( "Property is wrong", "fred", node1.getProperty( DictionaryConst.PROPERTY_AUTHOR ) );

    node1.removeProperty( DictionaryConst.PROPERTY_AUTHOR );
    assertEquals( "Property is wrong", null, node1.getProperty( DictionaryConst.PROPERTY_AUTHOR ) );

    node1.removeLink( node1.getLinks().get( 0 ) );
    assertEquals( "Links count is wrong", 0, node1.getLinks().size() );
  }

  @Test
  public void testLinkTypes() {
    assertTrue( "Link type is wrong", DictionaryHelper.isStructuralLinkType( DictionaryConst.LINK_CONTAINS ) );
    assertFalse( "Link type is wrong", DictionaryHelper.isDataFlowLinkType( DictionaryConst.LINK_CONTAINS ) );
  }

  @Test
  public void testCategoryColors() {
    assertEquals( "Color is wrong", DictionaryConst.COLOR_DATASOURCE,
      DictionaryHelper.getColorForCategory( DictionaryConst.CATEGORY_DATASOURCE ) );
    assertEquals( "Color is wrong", DictionaryConst.COLOR_OTHER, DictionaryHelper.getColorForCategory( "bogus" ) );
  }

  @Test
  public void testRegisterEntityType() {
    // get the current size of ENTITY_NODE_TYPES
    int numEntityNodes = DictionaryHelper.ENTITY_NODE_TYPES.size();

    assertEquals( numEntityNodes, DictionaryHelper.ENTITY_NODE_TYPES.size() );
    DictionaryHelper.registerEntityType( null );
    assertEquals( numEntityNodes, DictionaryHelper.ENTITY_NODE_TYPES.size() );
    DictionaryHelper.registerEntityType( "" );
    assertEquals( numEntityNodes, DictionaryHelper.ENTITY_NODE_TYPES.size() );
    DictionaryHelper.registerEntityType( " " );
    assertEquals( numEntityNodes, DictionaryHelper.ENTITY_NODE_TYPES.size() );
    DictionaryHelper.registerEntityType( "newEntityType" );
    assertTrue( DictionaryHelper.ENTITY_NODE_TYPES.contains( "newEntityType" ) );
    assertEquals( numEntityNodes + 1, DictionaryHelper.ENTITY_NODE_TYPES.size() );
  }

  @Test
  public void testIsEntityType() {
    DictionaryHelper.ENTITY_NODE_TYPES.remove( "newEntityType" );
    assertFalse( DictionaryHelper.ENTITY_NODE_TYPES.contains( "noEntityType" ) );
    DictionaryHelper.registerEntityType( "newEntityType" );
    assertTrue( DictionaryHelper.ENTITY_NODE_TYPES.contains( "newEntityType" ) );
    assertFalse( DictionaryHelper.isEntityType( "noEntityType" ) );
    assertTrue( DictionaryHelper.isEntityType( "newEntityType" ) );
  }

  @Test
  public void testEntityTypeRegistration () {

    // get the current sizes of ENTITY_NODE_TYPES and entityLinkTypes
    int numEntityNodes = DictionaryHelper.ENTITY_NODE_TYPES.size();
    int numLinkTypes = DictionaryHelper.getEntityLinkTypes().size();

    assertEquals( numEntityNodes, DictionaryHelper.ENTITY_NODE_TYPES.size() );
    assertEquals( numLinkTypes, DictionaryHelper.getEntityLinkTypes().size() );

    // File > isparent > JSON File
    DictionaryHelper.registerEntityType( "isparent", "JSON File", "File" );
    assertEquals( numEntityNodes + 1, DictionaryHelper.ENTITY_NODE_TYPES.size() );
    assertTrue( DictionaryHelper.ENTITY_NODE_TYPES.contains( "JSON File" ) );
    assertEquals( numLinkTypes + 1, DictionaryHelper.getEntityLinkTypes().size() );
    assertTrue( DictionaryHelper.getEntityLinkTypes().contains( "isparent" ) );
    assertEquals( "File", DictionaryHelper.getParentEntityNodeType( "isparent", "JSON File" ) );
    assertFalse( DictionaryHelper.linksToRoot( "isparent", "File" ) );
    assertFalse( DictionaryHelper.linksToRoot( "isparent", "JSON File" ) );

    // null > isparent > File
    DictionaryHelper.registerEntityType( "isparent", "File", null );
    assertEquals( numEntityNodes + 2, DictionaryHelper.ENTITY_NODE_TYPES.size() );
    assertTrue( DictionaryHelper.ENTITY_NODE_TYPES.contains( "File" ) );
    assertEquals( numLinkTypes + 1, DictionaryHelper.getEntityLinkTypes().size() );
    assertNull( DictionaryHelper.getParentEntityNodeType( "isparent", "File" ) );
    assertTrue( DictionaryHelper.linksToRoot( "isparent", "File" ) );

    // File > contains > Field
    DictionaryHelper.registerEntityType( "contains", "Field", "File" );
    assertEquals( numEntityNodes + 3, DictionaryHelper.ENTITY_NODE_TYPES.size() );
    assertTrue( DictionaryHelper.ENTITY_NODE_TYPES.contains( "Field" ) );
    assertEquals( numLinkTypes + 2, DictionaryHelper.getEntityLinkTypes().size() );
    assertTrue( DictionaryHelper.getEntityLinkTypes().contains( "contains" ) );
    assertEquals( "File", DictionaryHelper.getParentEntityNodeType( "contains", "Field" ) );
    assertNull( DictionaryHelper.getParentEntityNodeType( "isparent", "Field" ) );
    assertFalse( DictionaryHelper.linksToRoot( "isparent", "Field" ) );
    assertFalse( DictionaryHelper.linksToRoot( "contains", "Field" ) );

  }

}

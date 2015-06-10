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

package org.pentaho.dictionary;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.Trans;
import org.pentaho.metaverse.api.IMetaverseNode;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

public class DictionaryHelperTest {

  @Before
  public void init() {
    Set<Class> classes = new HashSet<>();
    classes.add( Trans.class );
    Set<String> types = new HashSet<>();
    types.add( DictionaryConst.NODE_TYPE_TRANS );
    classes = new HashSet<>();
    classes.add( Object.class );
    types = new HashSet<>();
    types.add( DictionaryConst.NODE_TYPE_TRANS_STEP );
    types.add( DictionaryConst.NODE_TYPE_TRANS_FIELD );
    types.add( DictionaryConst.NODE_TYPE_JOB );
    types.add( DictionaryConst.NODE_TYPE_JOB_ENTRY );
    types.add( DictionaryConst.NODE_TYPE_DATASOURCE );
    types.add( DictionaryConst.NODE_TYPE_DATA_TABLE );
    types.add( DictionaryConst.NODE_TYPE_DATA_COLUMN );
    types.add( DictionaryConst.NODE_TYPE_FILE );
    types.add( DictionaryConst.NODE_TYPE_FILE_FIELD );
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
    assertFalse( DictionaryHelper.ENTITY_NODE_TYPES.contains( "newEntityType" ) );
    DictionaryHelper.registerEntityType( "newEntityType" );
    assertTrue( DictionaryHelper.ENTITY_NODE_TYPES.contains( "newEntityType" ) );
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

}

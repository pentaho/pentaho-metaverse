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

package com.pentaho.dictionary;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.Trans;
import com.pentaho.metaverse.api.IMetaverseNode;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

public class DictionaryHelperTest {

  @Before
  public void init() {
    Set<Class> classes = new HashSet<Class>();
    classes.add( Trans.class );
    Set<String> types = new HashSet<String>();
    types.add( DictionaryConst.NODE_TYPE_TRANS );
    classes = new HashSet<Class>();
    classes.add( Object.class );
    types = new HashSet<String>();
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

    IMetaverseNode fieldNode = DictionaryHelper.addChildNode(
        DictionaryConst.NODE_TYPE_TRANS_FIELD + "~my transform.ktr~Table Input~Country",
        "Country", DictionaryConst.NODE_TYPE_TRANS_FIELD, null, stepNode, DictionaryConst.LINK_CREATES );

    MetaverseTransientNode node1 = (MetaverseTransientNode) transNode;
    MetaverseTransientNode node2 = (MetaverseTransientNode) stepNode;
    MetaverseTransientNode node3 = (MetaverseTransientNode) fieldNode;

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

}

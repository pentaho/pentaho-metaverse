package com.pentaho.dictionary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.Trans;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

@SuppressWarnings( "all" )
public class DictionaryHelperTest {

  @Before
  public void init() {
    IIdGenerator idGenerator = new GenericIdGenerator( DictionaryConst.NODE_TYPE_TRANS );
    Set<Class> classes = new HashSet<Class>();
    classes.add( Trans.class );
    Set<String> types = new HashSet<String>();
    types.add( DictionaryConst.NODE_TYPE_TRANS );
    DictionaryHelper.addIdGenerator( types, classes, idGenerator );
    idGenerator = new GenericIdGenerator( "default" );
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
    DictionaryHelper.addIdGenerator( types, classes, idGenerator );
  }

  @Test
  public void testGetIdGenerator() throws Exception {

    String id = DictionaryHelper.getId( DictionaryConst.NODE_TYPE_TRANS, "my transform.ktr" );
    assertNotNull( "Id is null", id );
    assertTrue( id.startsWith( DictionaryConst.NODE_TYPE_TRANS ) );

    id = DictionaryHelper.getId( "bogus", "bogus" );
    assertNull( "Id is not null", id );

    id = DictionaryHelper.getId( Trans.class, "my transform.ktr" );
    assertNotNull( "Id is null", id );
    assertTrue( id.startsWith( DictionaryConst.NODE_TYPE_TRANS ) );

    id = DictionaryHelper.getId( List.class, "my transform.ktr" );
    assertNull( "Id is not null", id );

    id = DictionaryHelper.getId( new Trans(), "my transform.ktr" );
    assertNotNull( "Id is null", id );
    assertTrue( id.startsWith( DictionaryConst.NODE_TYPE_TRANS ) );

    id = DictionaryHelper.getId( DictionaryConst.NODE_TYPE_FILE, "my file.txt" );
    assertNotNull( "Id is null", id );
    assertTrue( id.startsWith( "default" ) );

    id = DictionaryHelper.getId( new File( "my file.txt" ), "my file.txt" );
    assertNotNull( "Id is null", id );
    assertTrue( id.startsWith( "default" ) );

  }

  @Test
  public void testNodesAndLinks() {

    Properties props = new Properties();
    props.put( DictionaryConst.PROPERTY_AUTHOR, "fred" );
    props.put( DictionaryConst.PROPERTY_LAST_MODIFED, "2014-07-10 17:34:45" );
    IMetaverseNode transNode = DictionaryHelper.createMetaverseNode(
        DictionaryHelper.getId( DictionaryConst.NODE_TYPE_TRANS, "my transform.ktr" ),
        "my transform", DictionaryConst.NODE_TYPE_TRANS, props );

    IMetaverseNode stepNode = DictionaryHelper.addChildNode(
        DictionaryHelper.getId( DictionaryConst.NODE_TYPE_TRANS_STEP, "my transform.ktr", "Table Input" ),
        "Table Input", DictionaryConst.NODE_TYPE_TRANS_STEP, null, transNode, DictionaryConst.LINK_CONTAINS );

    IMetaverseNode fieldNode = DictionaryHelper.addChildNode(
        DictionaryHelper.getId( DictionaryConst.NODE_TYPE_TRANS_FIELD, "my transform.ktr", "Table Input", "Country" ),
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
    assertEquals( "Color is wrong", DictionaryConst.COLOR_DATASOURCE, DictionaryHelper.getColorForCategory( DictionaryConst.CATEGORY_DATASOURCE ) );
    assertEquals( "Color is wrong", DictionaryConst.COLOR_OTHER, DictionaryHelper.getColorForCategory( "bogus" ) );
  }

  @Test
  public void testDictionaryConstConstructorToSatisfyCodeCoverage() {
    DictionaryConst dc = new DictionaryConst();
    assertNotNull( dc );
  }

}

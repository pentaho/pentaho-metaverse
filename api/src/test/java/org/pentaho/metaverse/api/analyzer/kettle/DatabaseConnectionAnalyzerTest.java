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

package org.pentaho.metaverse.api.analyzer.kettle;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepDatabaseConnectionAnalyzer;
import org.pentaho.metaverse.api.testutils.MetaverseTestUtils;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class tests the DatabaseConnectionAnalyzer methods.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatabaseConnectionAnalyzerTest {

  DatabaseConnectionAnalyzer dbConnectionAnalyzer;

  @Mock
  private DatabaseMeta databaseMeta;

  @Mock
  private IMetaverseBuilder builder;

  @Mock
  private INamespace namespace;

  @Mock
  private IComponentDescriptor mockDescriptor;

  @Mock
  private BaseStepMeta baseStepMeta;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {

    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( builder.getMetaverseObjectFactory() ).thenReturn( factory );

    dbConnectionAnalyzer = new StepDatabaseConnectionAnalyzer();
    dbConnectionAnalyzer.setMetaverseBuilder( builder );
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testSetMetaverseBuilder() {
    assertNotNull( dbConnectionAnalyzer.metaverseBuilder );
  }

  @Test
  public void testSetMetaverseObjectFactory() {
    assertNotNull( dbConnectionAnalyzer.metaverseObjectFactory );
  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testSetMetaverseBuilderNull() throws MetaverseAnalyzerException {

    dbConnectionAnalyzer.setMetaverseBuilder( null );
    dbConnectionAnalyzer.analyze( mockDescriptor, databaseMeta );
  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testSetMetaverseBuilderNullMetaverseObjectFactory() throws MetaverseAnalyzerException {
    dbConnectionAnalyzer.metaverseObjectFactory = null;
    dbConnectionAnalyzer.analyze( mockDescriptor, databaseMeta );
  }

  @Test
  public void testAnalyze() {
    when( builder.addNode( any( IMetaverseNode.class ) ) ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        // add the logicalId to the node like it does in the real builder
        IMetaverseNode node = (IMetaverseNode)args[0];
        node.setProperty( DictionaryConst.PROPERTY_LOGICAL_ID, node.getLogicalId() );
        return builder;
      }
    } );
    try {
      IMetaverseNode node = dbConnectionAnalyzer.analyze( mockDescriptor, databaseMeta );
      assertNotNull( node );
      assertEquals( 13, node.getPropertyKeys().size() );
    } catch ( MetaverseAnalyzerException e ) {
      fail( "analyze() should not throw an exception!" );
    }

  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testNullAnalyze() throws MetaverseAnalyzerException {

    dbConnectionAnalyzer.analyze( null, null );

  }

  @Test
  public void testGetUsedConnections_nullUsedDatabaseMetas() throws Exception {
    when( baseStepMeta.getUsedDatabaseConnections() ).thenReturn( null );
    assertNull( dbConnectionAnalyzer.getUsedConnections( baseStepMeta ) );
  }

  @Test
  public void testGetUsedConnections_nullBaseStepMeta() throws Exception {
    assertNull( dbConnectionAnalyzer.getUsedConnections( null ) );
  }

  @Test
  public void testGetUsedConnections() throws Exception {
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( baseStepMeta.getUsedDatabaseConnections() ).thenReturn( new DatabaseMeta[]{ dbMeta } );
    List<DatabaseMeta> dbMetaList = dbConnectionAnalyzer.getUsedConnections( baseStepMeta );
    assertEquals( 1, dbMetaList.size() );
    assertEquals( dbMeta, dbMetaList.get( 0 ) );
  }

  @Test
  public void testBuildComponentDescriptor() throws Exception {
    when( databaseMeta.getName() ).thenReturn( "connectionName" );
    when( mockDescriptor.getNamespace() ).thenReturn( mock( INamespace.class) );
    when( mockDescriptor.getContext() ).thenReturn( mock( IAnalysisContext.class ) );
    IComponentDescriptor dbDesc = dbConnectionAnalyzer.buildComponentDescriptor( mockDescriptor, databaseMeta );
    assertNotNull( dbDesc );
    assertEquals( "connectionName", dbDesc.getName() );

  }
}

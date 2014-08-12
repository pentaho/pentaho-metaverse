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

package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.step.IStepAnalyzerProvider;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.impl.MetaverseNamespace;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @See com.pentaho.analyzer.kettle.MetaverseDocumentAnalyzerTest for base TransformationAnalyzer tests. Tests here
 * are specific to the TransformationAnalyzer.
 */
@RunWith( MockitoJUnitRunner.class )
public class TransformationAnalyzerTest {

  private TransformationAnalyzer analyzer;

  @Mock
  private TransMeta mockContent;

  @Mock
  private StepMeta mockStepMeta;

  @Mock
  private RowGeneratorMeta mockGenRowsStepMeta;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  private IMetaverseDocument mockTransDoc;

  @Mock
  private IStepAnalyzerProvider stepAnalyzerProvider;

  @Mock
  private MetaverseNamespace namespace;

  private IMetaverseComponentDescriptor descriptor;

  /**
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }

  }

  /**
   * @throws Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( factory );

    analyzer = new TransformationAnalyzer();
    analyzer.setMetaverseBuilder( mockBuilder );
    when( namespace.getChildNamespace( anyString(), anyString() ) ).thenReturn( namespace );
    when( namespace.getParentNamespace() ).thenReturn( namespace );

    when( mockTransDoc.getType() ).thenReturn( DictionaryConst.NODE_TYPE_TRANS );
    when( mockTransDoc.getContent() ).thenReturn( mockContent );
    when( mockTransDoc.getNamespace() ).thenReturn( namespace );

    when( mockGenRowsStepMeta.getParentStepMeta() ).thenReturn( mockStepMeta );

    when( mockStepMeta.getStepMetaInterface() ).thenReturn( mockGenRowsStepMeta );
    when( mockStepMeta.getParentTransMeta() ).thenReturn( mockContent );

    when( mockContent.nrSteps() ).thenReturn( 1 );
    when( mockContent.getStep( 0 ) ).thenReturn( mockStepMeta );

    descriptor = new MetaverseComponentDescriptor( "name", DictionaryConst.NODE_TYPE_TRANS, namespace );

  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testAnalyzerTransformWithSteps() throws MetaverseAnalyzerException {

    // increases line code coverage by adding step to transformation
    IMetaverseNode node = analyzer.analyze( descriptor, mockTransDoc );
    assertNotNull( node );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullMetaverseObjectFactory() throws MetaverseAnalyzerException {
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( null );
    analyzer.setMetaverseBuilder( mockBuilder );
    analyzer.analyze( descriptor, mockTransDoc );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithBadXML() throws MetaverseAnalyzerException {
    IMetaverseDocument newMockTransDoc = mock( IMetaverseDocument.class );
    when( newMockTransDoc.getType() ).thenReturn( DictionaryConst.NODE_TYPE_TRANS );
    when( newMockTransDoc.getContent() ).thenReturn(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<transformation>This is not a valid TransMeta doc!" );
    analyzer.analyze( descriptor, newMockTransDoc );
  }

  @Test
  public void testGetBaseStepMetaFromStepMetaWithNull() {
    // BaseStepMeta should not be null, but its parent should be
    BaseStepMeta baseStepMeta = analyzer.getBaseStepMetaFromStepMeta( null );
    assertNotNull( baseStepMeta );
    assertNull( baseStepMeta.getParentStepMeta() );
  }

  @Test
  public void testGetBaseStepMetaFromStepMetaInterfaceNotABaseStepMeta() throws MetaverseAnalyzerException {
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( mock( StepMetaInterface.class ) );
    analyzer.analyze( descriptor, mockTransDoc );
  }

  @Test
  public void testAnalyzeStepWithNullParentTransMeta() throws MetaverseAnalyzerException {
    when( mockStepMeta.getParentTransMeta() ).thenReturn( null );
    analyzer.analyze( descriptor, mockTransDoc );
  }

  @Test
  public void testAnalyzeStepsWithNullStepMeta() throws MetaverseAnalyzerException {
    when( mockContent.getStep( 0 ) ).thenReturn( null );
    analyzer.analyze( descriptor, mockTransDoc );
  }

  @Test
  public void testAnalyzeStepsWithAnalyzerProvider() throws MetaverseAnalyzerException {
    analyzer.setStepAnalyzerProvider( stepAnalyzerProvider );
    analyzer.analyze( descriptor, mockTransDoc );
  }

  @Test
  public void testSetStepAnalyzerProvider() {
    analyzer.setStepAnalyzerProvider( stepAnalyzerProvider );
    assertEquals( analyzer.getStepAnalyzerProvider(), stepAnalyzerProvider );
    analyzer.setStepAnalyzerProvider( null );
    assertNull( analyzer.getStepAnalyzerProvider() );
  }

  @Test
  public void testGetStepAnalyzersWithNullBaseStepMeta() {
    TransformationAnalyzer spyAnalyzer = spy( analyzer );
    when( spyAnalyzer.getBaseStepMetaFromStepMeta( mockStepMeta ) ).thenReturn( null );
    spyAnalyzer.setStepAnalyzerProvider( stepAnalyzerProvider );
    spyAnalyzer.getStepAnalyzers( mockStepMeta );
  }
}

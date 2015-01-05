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
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.impl.MetaverseDocument;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IDocument;
import org.pentaho.platform.api.metaverse.IDocumentAnalyzer;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class MetaverseDocumentAnalyzerTest {

  static {

    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }

  @Parameterized.Parameters
  public static Collection services() {
    return Arrays.asList( new Object[][] {
        { new TransformationAnalyzer(), DictionaryConst.NODE_TYPE_TRANS, new TransMeta() },
        { new JobAnalyzer(), DictionaryConst.NODE_TYPE_JOB, new JobMeta() }
    } );
  }

  private IDocumentAnalyzer analyzer;

  private String type;

  private XMLInterface content;

  private IMetaverseBuilder builder;

  private IDocument transDoc;

  private IMetaverseObjectFactory factory;

  private INamespace namespace;

  private IComponentDescriptor descriptor;

  /**
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
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
    builder = mock( IMetaverseBuilder.class );
    transDoc = mock( IDocument.class );
    namespace = mock( INamespace.class );
    descriptor = new MetaverseComponentDescriptor( "name", DictionaryConst.NODE_TYPE_TRANS, namespace );

    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( builder.getMetaverseObjectFactory() ).thenReturn( factory );

    analyzer.setMetaverseBuilder( builder );
    when( namespace.getParentNamespace() ).thenReturn( namespace );

    when( transDoc.getType() ).thenReturn( type );
    when( transDoc.getContent() ).thenReturn( content );
    when( transDoc.getNamespace() ).thenReturn( namespace );

  }

  public MetaverseDocumentAnalyzerTest( IDocumentAnalyzer analyzer, String type, XMLInterface content ) {
    this.analyzer = analyzer;
    this.type = type;
    this.content = content;
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testNullAnalyze() throws MetaverseAnalyzerException {

    analyzer.analyze( null, null );

  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testNullDocumentContent() throws MetaverseAnalyzerException {

    when( transDoc.getContent() ).thenReturn( null );
    analyzer.analyze( descriptor, transDoc );

  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testAnalyzeNonTransDocument() throws MetaverseAnalyzerException {

    analyzer.analyze( descriptor, new MetaverseDocument() );

  }

  @Test
  public void testAnalyzeTransDocument() throws MetaverseAnalyzerException {

    IMetaverseNode node = (IMetaverseNode) analyzer.analyze( descriptor, transDoc );
    assertNotNull( node );

  }

  @Test
  public void testGetSupportedTypes() {

    Set<String> types = analyzer.getSupportedTypes();
    assertNotNull( types );

  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testSetMetaverseBuilderNull() throws MetaverseAnalyzerException {

    analyzer.setMetaverseBuilder( null );
    analyzer.analyze( descriptor, transDoc );

  }

  @Test
  public void testAnalyzeContentFromXml() throws MetaverseAnalyzerException, KettleException {

    when( transDoc.getContent() ).thenReturn( content.getXML() );

    IMetaverseNode node = (IMetaverseNode) analyzer.analyze( descriptor, transDoc );
    assertNotNull( node );

  }

}

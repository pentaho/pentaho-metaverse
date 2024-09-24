/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle;

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
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentAnalyzer;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.MetaverseDocument;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

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

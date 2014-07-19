package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.impl.MetaverseDocument;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.metaverse.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith( Parameterized.class )
public class MetaverseDocumentAnalyzerTest {

  @Parameterized.Parameters
  public static Collection services() {
    return Arrays.asList( new Object[][] {
        { new TransformationAnalyzer(), DictionaryConst.NODE_TYPE_TRANS, new TransMeta(  ) },
        { new JobAnalyzer(), DictionaryConst.NODE_TYPE_JOB, new JobMeta(  ) }
    } );
  }

  private IDocumentAnalyzer analyzer;

  private String type;

  private Object content;

  private IMetaverseBuilder builder;

  private IMetaverseDocument transDoc;

  private IMetaverseObjectFactory factory;

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
    factory = MetaverseTestUtils.getMetaverseObjectFactory();
    builder = mock(IMetaverseBuilder.class);
    transDoc = mock(IMetaverseDocument.class);

    analyzer.setMetaverseBuilder( builder );
    analyzer.setMetaverseObjectFactory( factory );

  }

  public MetaverseDocumentAnalyzerTest( IDocumentAnalyzer analyzer, String type, Object content) {
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

  @Test( expected = MetaverseAnalyzerException.class )
  public void testNullAnalyze() throws MetaverseAnalyzerException {
    analyzer.analyze( null );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeNonTransDocument() throws MetaverseAnalyzerException {
    analyzer.analyze( new MetaverseDocument() );
  }

  @Test
  public void testAnalyzeTransDocument() throws MetaverseAnalyzerException {

    when( transDoc.getType() ).thenReturn( type);
    when( transDoc.getContent() ).thenReturn( content );

    IMetaverseNode node = analyzer.analyze( transDoc );
    assertNotNull( node );

  }

  @Test
  public void testGetSupportedTypes() {

    Set<String> types = analyzer.getSupportedTypes();
    assertNotNull( types );

  }


}

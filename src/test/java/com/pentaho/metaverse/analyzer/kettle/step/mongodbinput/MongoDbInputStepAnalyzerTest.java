package com.pentaho.metaverse.analyzer.kettle.step.mongodbinput;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.IConnectionAnalyzer;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mongodbinput.MongoDbInputMeta;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: RFellows Date: 3/6/15
 */
@RunWith( MockitoJUnitRunner.class )
public class MongoDbInputStepAnalyzerTest {
  MongoDbInputStepAnalyzer analyzer;

  @Mock private IMetaverseBuilder mockBuilder;
  @Mock private MongoDbInputMeta mongoDbInputMeta;
  @Mock private INamespace mockNamespace;
  @Mock private StepMeta parentStepMeta;
  @Mock private TransMeta mockTransMeta;

  IComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( factory );
    analyzer = new MongoDbInputStepAnalyzer();
    analyzer.setConnectionAnalyzer( mock( IConnectionAnalyzer.class ) );
    analyzer.setMetaverseBuilder( mockBuilder );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS, mockNamespace );

    when( mongoDbInputMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( parentStepMeta.getName() ).thenReturn( "test" );
    when( parentStepMeta.getStepID() ).thenReturn( "MongoDbInput" );

  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testNullAnalyze() throws MetaverseAnalyzerException {
    analyzer.analyze( descriptor, null );
  }

  @Test
  public void testAnalyze() throws Exception {
    when( mongoDbInputMeta.getJsonQuery() ).thenReturn( "{test:test}" );
    when( mongoDbInputMeta.getCollection() ).thenReturn( "myCollection" );
    IMetaverseNode node = analyzer.analyze( descriptor, mongoDbInputMeta );
    assertNotNull( node );

    assertEquals( "{test:test}", node.getProperty( DictionaryConst.PROPERTY_QUERY ) );
    assertEquals( "myCollection", node.getProperty( MongoDbInputStepAnalyzer.COLLECTION ) );
  }

  @Test
  public void testGetSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( MongoDbInputMeta.class ) );
  }

}


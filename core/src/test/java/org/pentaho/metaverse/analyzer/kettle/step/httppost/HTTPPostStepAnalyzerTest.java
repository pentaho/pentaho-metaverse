/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.step.httppost;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.httppost.HTTPPOST;
import org.pentaho.di.trans.steps.httppost.HTTPPOSTMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.step.ClonableStepAnalyzerTest;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class HTTPPostStepAnalyzerTest extends ClonableStepAnalyzerTest {

  private HTTPPostStepAnalyzer analyzer;

  @Mock HTTPPOST mockHTTPPost;
  @Mock HTTPPOSTMeta meta;
  @Mock StepNodes stepNodes;
  @Mock INamespace mockNamespace;

  IComponentDescriptor descriptor;

  @Mock StepMeta mockStepMeta;
  @Mock TransMeta mockTransMeta;
  @Mock RowMetaInterface mockRowMetaInterface;

  @Before
  public void setUp() throws Exception {

    analyzer = spy( new HTTPPostStepAnalyzer() );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
    analyzer.setDescriptor( descriptor );
    analyzer.setObjectFactory( MetaverseTestUtils.getMetaverseObjectFactory() );

    when( mockHTTPPost.getStepMetaInterface() ).thenReturn( meta );
    lenient().when( mockHTTPPost.getStepMeta() ).thenReturn( mockStepMeta );
    lenient().when( mockStepMeta.getStepMetaInterface() ).thenReturn( meta );
  }

  @Test
  public void testGetUsedFields_urlInField() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.isUrlInField() ).thenReturn( true );
    when( meta.getUrlField() ).thenReturn( "url" );
    doReturn( stepNodes ).when( analyzer ).getInputs();
    doReturn( fields ).when( analyzer ).createStepFields( "url", stepNodes );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );

    verify( analyzer ).createStepFields( "url", stepNodes );
  }

  @Test
  public void testGetUsedFields_urlInFieldNoFieldSet() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.isUrlInField() ).thenReturn( true );
    when( meta.getUrlField() ).thenReturn( null );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );

    verify( analyzer, never() ).createStepFields( anyString(), any( StepNodes.class ) );
  }

  @Test
  public void testGetUsedFields_getQueryField() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.getQueryField() ).thenReturn( new String[] { "param1", "param2" } );
    doReturn( stepNodes ).when( analyzer ).getInputs();
    doReturn( fields ).when( analyzer ).createStepFields( anyString(), eq( stepNodes ) );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );

    verify( analyzer ).createStepFields( "param1", stepNodes );
    verify( analyzer ).createStepFields( "param2", stepNodes );
  }

  @Test
  public void testGetUsedFields_getArgumentField() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.getArgumentField() ).thenReturn( new String[] { "header1", "header2" } );
    doReturn( stepNodes ).when( analyzer ).getInputs();
    doReturn( fields ).when( analyzer ).createStepFields( anyString(), eq( stepNodes ) );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );

    verify( analyzer ).createStepFields( "header1", stepNodes );
    verify( analyzer ).createStepFields( "header2", stepNodes );
  }

  @Test
  public void testGetInputRowMetaInterface() throws Exception {
    Map<String, RowMetaInterface> inputs = new HashMap<>();
    doReturn( inputs ).when( analyzer ).getInputFields( meta );

    Map<String, RowMetaInterface> inputRowMetaInterfaces = analyzer.getInputRowMetaInterfaces( meta );
    assertEquals( inputs, inputRowMetaInterfaces );
  }

  @Test
  public void testCreateResourceNode() throws Exception {
    IExternalResourceInfo res = mock( IExternalResourceInfo.class );
    when( res.getName() ).thenReturn( "http://my.rest.url" );

    IMetaverseNode resourceNode = analyzer.createResourceNode( res );
    assertNotNull( resourceNode );
    assertEquals( DictionaryConst.NODE_TYPE_WEBSERVICE, resourceNode.getType() );
    assertEquals( "http://my.rest.url", resourceNode.getName() );

  }

  @Test
  public void testGetResourceInputNodeType() throws Exception {
    assertEquals( DictionaryConst.NODE_TYPE_WEBSERVICE, analyzer.getResourceInputNodeType() );
  }

  @Test
  public void testGetResourceOutputNodeType() throws Exception {
    assertNull( analyzer.getResourceOutputNodeType() );
  }

  @Test
  public void testIsOutput() throws Exception {
    assertFalse( analyzer.isOutput() );
  }

  @Test
  public void testIsInput() throws Exception {
    assertTrue( analyzer.isInput() );
  }

  @Test
  public void testGetSupportedSteps() {
    HTTPPostStepAnalyzer analyzer = new HTTPPostStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( HTTPPOSTMeta.class ) );
  }

  @Test
  public void testHTTPPostExternalResourceConsumer() throws Exception {
    HTTPPostExternalResourceConsumer consumer = new HTTPPostExternalResourceConsumer();

    StepMeta meta = new StepMeta( "test", this.meta );
    StepMeta spyMeta = spy( meta );

    when( this.meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( this.meta.getUrl() ).thenReturn( "http://seylermartialarts.com" );
    when( this.meta.isUrlInField() ).thenReturn( false );

    assertTrue( consumer.isDataDriven( this.meta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( DefaultBowl.getInstance(), this.meta );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );


    when( this.meta.isUrlInField() ).thenReturn( true );
    assertTrue( consumer.isDataDriven( this.meta ) );
    assertTrue( consumer.getResourcesFromMeta( DefaultBowl.getInstance(), this.meta ).isEmpty() );
    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.any(), Mockito.any() ) )
      .thenReturn( "/path/to/row/file" );
    resources = consumer.getResourcesFromRow( mockHTTPPost, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.any(), Mockito.any() ) )
      .thenThrow( KettleValueException.class );
    resources = consumer.getResourcesFromRow( mockHTTPPost, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( HTTPPOSTMeta.class, consumer.getMetaClass() );
  }

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new HTTPPostStepAnalyzer();
  }
}

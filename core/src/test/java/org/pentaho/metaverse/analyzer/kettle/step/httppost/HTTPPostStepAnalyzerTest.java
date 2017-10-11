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

package org.pentaho.metaverse.analyzer.kettle.step.httppost;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.httppost.HTTPPOST;
import org.pentaho.di.trans.steps.httppost.HTTPPOSTMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class HTTPPostStepAnalyzerTest {

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
    when( mockHTTPPost.getStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( meta );
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
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( this.meta );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );


    when( this.meta.isUrlInField() ).thenReturn( true );
    assertTrue( consumer.isDataDriven( this.meta ) );
    assertTrue( consumer.getResourcesFromMeta( this.meta ).isEmpty() );
    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenReturn( "/path/to/row/file" );
    resources = consumer.getResourcesFromRow( mockHTTPPost, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenThrow( KettleException.class );
    resources = consumer.getResourcesFromRow( mockHTTPPost, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( HTTPPOSTMeta.class, consumer.getMetaClass() );
  }
}

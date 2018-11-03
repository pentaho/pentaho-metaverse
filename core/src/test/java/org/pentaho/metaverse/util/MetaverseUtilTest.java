/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.util;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentAnalyzer;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.IRequiresMetaverseBuilder;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operation;
import org.pentaho.metaverse.api.model.Operations;
import org.pentaho.metaverse.impl.MetaverseBuilder;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MetaverseUtilTest {

  @Before
  public void setUp() throws Exception {
    MetaverseUtil.setDocumentController( MetaverseTestUtils.getDocumentController() );
  }

  @Test
  public void testDefaultConstructor() {
    assertNotNull( new MetaverseUtil() );
  }

  @Test
  public void testGetDocumentController() throws Exception {
    IDocumentController documentController = MetaverseUtil.getDocumentController();
    assertNotNull( documentController );
    MetaverseUtil.documentController = null;
    assertNull( MetaverseUtil.getDocumentController() );

    // Generate an exception
    MetaverseBeanUtil instance = MetaverseBeanUtil.getInstance();
    BundleContext bundleContext = mock( BundleContext.class );
    Bundle bundle = mock( Bundle.class );
    when( bundleContext.getBundle() ).thenReturn( bundle );
    instance.setBundleContext( bundleContext );

    ServiceReference serviceReference = mock( ServiceReference.class );
    BlueprintContainer service = mock( BlueprintContainer.class );
    when( bundleContext.getService( Mockito.any( ServiceReference.class ) ) ).thenReturn( service );

    when( service.getComponentInstance( "IDocumentController" ) ).thenReturn( documentController );
    assertNull( MetaverseUtil.getDocumentController() );
  }

  @Test
  public void testCreateDocument() throws Exception {
    Object content = new Object();
    INamespace namespace = mock( INamespace.class );
    IDocument document = MetaverseUtil.createDocument(
      namespace,
      content,
      "myID",
      "myName",
      "myExtension",
      "application/text" );

    assertEquals( document.getNamespace(), namespace );
    assertEquals( document.getContent(), content );
    assertEquals( document.getStringID(), "myID" );
    assertEquals( document.getName(), "myName" );
    assertEquals( document.getMimeType(), "application/text" );

  }

  @Test( expected = MetaverseException.class )
  public void testAddLineageGraphNullDocument() throws Exception {
    MetaverseUtil.addLineageGraph( null, null );
  }

  @Test
  public void testAddLineageGraph() throws Exception {

    IDocument document = mock( IDocument.class );
    when( document.getName() ).thenReturn( "myDoc" );
    Object content = new Object();
    when( document.getContent() ).thenReturn( content );

    IDocumentController documentController =
      mock( IDocumentController.class, withSettings().extraInterfaces( IRequiresMetaverseBuilder.class ) );
    List<IDocumentAnalyzer> analyzers = new ArrayList<IDocumentAnalyzer>();
    when( documentController.getDocumentAnalyzers( Mockito.anyString() ) ).thenReturn( analyzers );

    MetaverseUtil.documentController = documentController;

    // Empty analyzer set
    MetaverseUtil.addLineageGraph( document, null );

    IDocumentAnalyzer<IMetaverseNode> documentAnalyzer = mock( IDocumentAnalyzer.class );
    when( documentAnalyzer.analyze(
      Mockito.any( IComponentDescriptor.class ), Mockito.any( IDocument.class ) ) )
      .thenReturn( mock( IMetaverseNode.class ) );
    analyzers.add( documentAnalyzer );

    Graph graph = new TinkerGraph();
    MetaverseUtil.addLineageGraph( document, graph );

    MetaverseUtil.addLineageGraph( document, null );
  }

  @Test
  public void testEnhanceEdge() {
    Graph graph = new TinkerGraph();
    Vertex v1 = graph.addVertex( 1 );
    Vertex v2 = graph.addVertex( 2 );
    Edge edge = graph.addEdge( 3, v1, v2, "testLabel" );
    MetaverseUtil.enhanceEdge( edge );
  }

  @Test
  public void testEnhanceVertex() {
    Graph graph = new TinkerGraph();
    Vertex v1 = graph.addVertex( 1 );
    MetaverseUtil.enhanceVertex( v1 );
  }

  @Test
  public void testConvertOperationsStringToMap() {
    // Test null string
    assertNull( MetaverseUtil.convertOperationsStringToMap( null ) );
    assertNull( MetaverseUtil.convertOperationsStringToMap( "" ) );
    assertNull( MetaverseUtil.convertOperationsStringToMap( "{" ) );
    assertNotNull( MetaverseUtil.convertOperationsStringToMap( "{}" ) );
    Operations ops = MetaverseUtil.convertOperationsStringToMap(
      "{\"metadataOperations\":[{\"category\":\"changeMetadata\",\"class\":"
        + "\"Operation\",\"description\":\"name\","
        + "\"name\":\"modified\",\"type\":\"METADATA\"}]}"
    );
    assertNotNull( ops );
    assertNull( ops.get( ChangeType.DATA ) );
    List<IOperation> metadataOps = ops.get( ChangeType.METADATA );
    assertNotNull( metadataOps );
    assertEquals( 1, metadataOps.size() );
    IOperation op = metadataOps.get( 0 );
    assertEquals( Operation.METADATA_CATEGORY, op.getCategory() );
    assertEquals( DictionaryConst.PROPERTY_MODIFIED, op.getName() );
    assertEquals( "name", op.getDescription() );

  }

  @Test
  public void tesBuildDocument() throws MetaverseException {
    final IMetaverseBuilder builder = new MetaverseBuilder( null );
    final AbstractMeta transMeta = new TransMeta();
    final String transName = "MyTransMeta";
    transMeta.setName( transName );
    final String id = "path.ktr";
    final String namespaceId = "MyNamespace";
    final INamespace namespace = new Namespace( namespaceId );

    assertNull( MetaverseUtil.buildDocument( null, transMeta, id, namespace ) );

    IDocument document = MetaverseUtil.buildDocument( builder, transMeta, id, namespace );
    assertNotNull( document );
    assertEquals( namespace, document.getNamespace() );
    assertEquals( transMeta, document.getContent() );
    assertEquals( id, document.getStringID() );
    assertEquals( transName, document.getName() );
    assertEquals( "ktr", document.getExtension() );
    assertEquals( DictionaryConst.CONTEXT_RUNTIME, document.getContext().getContextName() );
    assertEquals( document.getName(), document.getProperty( DictionaryConst.PROPERTY_NAME ) );
    assertEquals( KettleAnalyzerUtil.normalizeFilePath( "path.ktr" ), document.getProperty( DictionaryConst
      .PROPERTY_PATH ) );
    assertEquals(namespaceId, document.getProperty( DictionaryConst.PROPERTY_NAMESPACE ) );
  }
}

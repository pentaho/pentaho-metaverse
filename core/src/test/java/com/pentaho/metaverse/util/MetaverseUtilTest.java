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
package com.pentaho.metaverse.util;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operation;
import org.pentaho.metaverse.api.model.Operations;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
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
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentAnalyzer;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.IRequiresMetaverseBuilder;
import org.pentaho.metaverse.api.MetaverseException;

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
}

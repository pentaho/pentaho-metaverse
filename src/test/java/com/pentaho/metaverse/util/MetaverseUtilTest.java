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

import com.pentaho.metaverse.api.IDocumentController;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IDocument;
import org.pentaho.platform.api.metaverse.IDocumentAnalyzer;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.IRequiresMetaverseBuilder;
import org.pentaho.platform.api.metaverse.MetaverseException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

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
    Set<IDocumentAnalyzer> analyzers = new HashSet<IDocumentAnalyzer>();
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
}

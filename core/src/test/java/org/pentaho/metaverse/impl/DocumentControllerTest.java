/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.metaverse.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.metaverse.api.IAnalyzer;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentAnalyzer;
import org.pentaho.metaverse.api.IDocumentEvent;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseLink;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class DocumentControllerTest {

  private DocumentController docController;
  private List<IDocumentAnalyzer> analyzers;

  @Mock
  private IDocumentAnalyzer dummyAnalyzer;
  @Mock
  private IDocumentAnalyzer testAndDummyAnalyzer;
  @Mock
  private IDocumentAnalyzer anotherAnalyzer;
  @Mock
  private IMetaverseBuilder mockBuilder;
  @Mock
  private IMetaverseNode mockNode;
  @Mock
  private IMetaverseLink mockLink;
  @Mock
  private IDocumentEvent mockEvent;
  @Mock
  private IDocument mockDoc;
  @Mock
  private IComponentDescriptor mockDescriptor;

  @Before
  public void setup() {
    docController = new DocumentController();

    IMetaverseBuilder builder = mock( IMetaverseBuilder.class );
    docController.setMetaverseBuilder( builder );
    IMetaverseObjectFactory objectFactory = mock( IMetaverseObjectFactory.class );
    docController.setMetaverseObjectFactory( objectFactory );

    initAnalyzers();
  }

  @After
  public void tearDown() throws Exception {
    MetaverseCompletionService.getInstance().waitTillEmpty();
  }

  private void initAnalyzers() {
    analyzers = new ArrayList<IDocumentAnalyzer>();
    Set<String> supportedTypes1 = new HashSet<String>();
    supportedTypes1.add( "dummy" );
    when( dummyAnalyzer.getSupportedTypes() ).thenReturn( supportedTypes1 );

    Set<String> supportedTypes2 = new HashSet<String>();
    supportedTypes2.add( "test" );
    supportedTypes2.add( "dummy" );
    when( testAndDummyAnalyzer.getSupportedTypes() ).thenReturn( supportedTypes2 );

    Set<String> supportedTypes3 = new HashSet<String>();
    supportedTypes3.add( "another" );
    when( anotherAnalyzer.getSupportedTypes() ).thenReturn( null );

    analyzers.add( dummyAnalyzer );
    analyzers.add( testAndDummyAnalyzer );
    analyzers.add( anotherAnalyzer );

    docController.setDocumentAnalyzers( analyzers );
  }

  @Test
  public void testRemoveAnalyzer_multipleWithSameType() throws Exception {
    List<IDocumentAnalyzer> analyzers = docController.getDocumentAnalyzers( "test" );
    assertEquals( 1, analyzers.size() );

    docController.removeAnalyzer( testAndDummyAnalyzer );

    analyzers = docController.getDocumentAnalyzers( "test" );
    assertNull( analyzers );
  }

  @Test
  public void testRemoveAnalyzer() throws Exception {
    List<IDocumentAnalyzer> analyzers = docController.getDocumentAnalyzers( "dummy" );
    assertEquals( 2, analyzers.size() );

    docController.removeAnalyzer( dummyAnalyzer );

    analyzers = docController.getDocumentAnalyzers( "dummy" );
    assertEquals( 1, analyzers.size() );
  }

  @Test
  public void testGetDocumentAnalyzers() {
    List<IDocumentAnalyzer> analyzers = docController.getAnalyzers();
    assertEquals( this.analyzers.size(), analyzers.size() );
  }

  @Test
  public void testGetAnalyzers_nullSet() {
    assertNotNull( docController.getAnalyzers( null ) );
  }

  @Test
  public void testGetAnalyzers_OnlyDocAnalyzersSet() {
    Set<Class<?>> types = new HashSet<Class<?>>();
    types.add( IDocumentAnalyzer.class );

    List<IDocumentAnalyzer> documentAnalyzers = docController.getAnalyzers( types );
    assertNotNull( documentAnalyzers );
    assertEquals( docController.getAnalyzers(), documentAnalyzers );
  }

  @Test
  public void testGetAnalyzers_mutipleAnalyzersSet() {
    Set<Class<?>> types = new HashSet<Class<?>>();
    types.add( IDocumentAnalyzer.class );
    types.add( IAnalyzer.class );

    List<IDocumentAnalyzer> documentAnalyzers = docController.getAnalyzers( types );
    assertNull( documentAnalyzers );
  }

  @Test
  public void testGetAnalyzers_notDocAnalyzerSet() {
    Set<Class<?>> types = new HashSet<Class<?>>();
    types.add( IAnalyzer.class );

    List<IDocumentAnalyzer> documentAnalyzers = docController.getAnalyzers( types );
    assertNull( documentAnalyzers );
  }

  @Test
  public void testGetDocumentAnalyzersForType() {
    List<IDocumentAnalyzer> analyzers = docController.getDocumentAnalyzers( "dummy" );
    assertEquals( 2, analyzers.size() );

    analyzers = docController.getDocumentAnalyzers( "test" );
    assertEquals( 1, analyzers.size() );

    // null type should return everything
    analyzers = docController.getDocumentAnalyzers( null );
    assertEquals( this.analyzers.size(), analyzers.size() );
  }

  @Test
  public void testOnEvent() throws Exception {
    when( mockEvent.getDocument() ).thenReturn( mockDoc );
    when( mockDoc.getExtension() ).thenReturn( "dummy" );

    docController.onEvent( mockEvent );
    MetaverseCompletionService.getInstance().waitTillEmpty();

    verify( dummyAnalyzer ).analyze( any( IComponentDescriptor.class ), eq( mockDoc ) );
    verify( testAndDummyAnalyzer ).analyze( any( IComponentDescriptor.class ), eq( mockDoc ) );
  }

  @Test
  public void testOnEvent_notAllAnalyzersFire() throws Exception {
    when( mockEvent.getDocument() ).thenReturn( mockDoc );
    when( mockDoc.getExtension() ).thenReturn( "test" );

    docController.onEvent( mockEvent );
    MetaverseCompletionService.getInstance().waitTillEmpty();

    verify( dummyAnalyzer, never() ).analyze( any( IComponentDescriptor.class ), eq( mockDoc ) );
    verify( testAndDummyAnalyzer ).analyze( any( IComponentDescriptor.class ), eq( mockDoc ) );
    verify( anotherAnalyzer, never() ).analyze( any( IComponentDescriptor.class ), eq( mockDoc ) );
  }

  @Test
  public void testOnEvent_noSupportingAnalyzers() throws Exception {
    when( mockEvent.getDocument() ).thenReturn( mockDoc );
    when( mockDoc.getExtension() ).thenReturn( "notSupported" );

    docController.onEvent( mockEvent );
    MetaverseCompletionService.getInstance().waitTillEmpty();

    // make sure noe of the analyzers are fired for this un supported type
    verify( dummyAnalyzer, never() ).analyze( mockDescriptor, mockDoc );
    verify( anotherAnalyzer, never() ).analyze( mockDescriptor, mockDoc );
    verify( testAndDummyAnalyzer, never() ).analyze( mockDescriptor, mockDoc );
  }

  @Test
  public void testBuilderPassthroughCalls() {
    docController = new DocumentController( mockBuilder );

    docController.addNode( mockNode );
    docController.addLink( mockLink );
    docController.updateNode( mockNode );
    docController.updateLinkLabel( mockLink, "myLabel" );
    docController.deleteLink( mockLink );
    docController.deleteNode( mockNode );
    docController.addLink( mockNode, "is self", mockNode );

    verify( mockBuilder, times( 1 ) ).addNode( mockNode );
    verify( mockBuilder, times( 1 ) ).addLink( mockLink );
    verify( mockBuilder, times( 1 ) ).updateNode( mockNode );
    verify( mockBuilder, times( 1 ) ).updateLinkLabel( mockLink, "myLabel" );
    verify( mockBuilder, times( 1 ) ).deleteNode( mockNode );
    verify( mockBuilder, times( 1 ) ).deleteLink( mockLink );
    verify( mockBuilder, times( 1 ) ).addLink( mockNode, "is self", mockNode );

  }

  @Test
  public void testGetMetaverseBuilder() {
    assertNotNull( docController.getMetaverseBuilder() );
  }

  @Test
  public void testSetMetaverseObjectFactory_nullBuilder() throws Exception {
    DocumentController dc = new DocumentController( null );
    IMetaverseObjectFactory factory = mock( IMetaverseObjectFactory.class );
    dc.setMetaverseObjectFactory( factory );
    verify( mockBuilder, never() ).setMetaverseObjectFactory( any( IMetaverseObjectFactory.class ) );
  }

  @Test
  public void testGetMetaverseObjectFactory_nullBuilder() throws Exception {
    DocumentController dc = new DocumentController( null );
    IMetaverseObjectFactory factory = mock( IMetaverseObjectFactory.class );
    assertNull( dc.getMetaverseObjectFactory() );
    verify( mockBuilder, never() ).getMetaverseObjectFactory();
  }

  @Test
  public void testGetMetaverseObjectFactory() throws Exception {
    IMetaverseObjectFactory objectFactory = mock( IMetaverseObjectFactory.class );
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( objectFactory );
    docController.setMetaverseBuilder( mockBuilder );
    // the docController might have its own factory copy
    assertNotEquals( mockBuilder.getMetaverseObjectFactory(), docController.getMetaverseObjectFactory() );
    // ... but when this factory is null, we return the factory associated with the metaverseBuilder
    docController.setMetaverseObjectFactory( null );
    assertEquals( mockBuilder.getMetaverseObjectFactory(), docController.getMetaverseObjectFactory() );
  }

}

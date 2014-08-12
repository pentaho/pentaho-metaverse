package com.pentaho.metaverse.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.metaverse.IAnalyzer;
import org.pentaho.platform.api.metaverse.IDocumentAnalyzer;
import org.pentaho.platform.api.metaverse.IDocumentEvent;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.timeout;

@RunWith( MockitoJUnitRunner.class )
public class DocumentControllerTest {

  private DocumentController docController;
  private Set<IDocumentAnalyzer> analyzers;

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
  private IMetaverseDocument mockDoc;
  @Mock
  private IMetaverseComponentDescriptor mockDescriptor;

  @Before
  public void setup() {
    docController = new DocumentController();

    IMetaverseBuilder builder = mock( IMetaverseBuilder.class );
    docController.setMetaverseBuilder( builder );

    IMetaverseObjectFactory objectFactory = mock( IMetaverseObjectFactory.class );
    docController.setMetaverseObjectFactory( objectFactory );

    initAnalyzers();
  }

  private void initAnalyzers() {
    analyzers = new HashSet<IDocumentAnalyzer>();
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
  public void testGetDocumentAnalyzers() {
    Set<IDocumentAnalyzer> analyzers = docController.getAnalyzers();
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

    Set<IDocumentAnalyzer> documentAnalyzers = docController.getAnalyzers( types );
    assertNotNull( documentAnalyzers );
    assertEquals( docController.getAnalyzers(), documentAnalyzers );
  }

  @Test
  public void testGetAnalyzers_mutipleAnalyzersSet() {
    Set<Class<?>> types = new HashSet<Class<?>>();
    types.add( IDocumentAnalyzer.class );
    types.add( IAnalyzer.class );

    Set<IDocumentAnalyzer> documentAnalyzers = docController.getAnalyzers( types );
    assertNull( documentAnalyzers );
  }

  @Test
  public void testGetAnalyzers_notDocAnalyzerSet() {
    Set<Class<?>> types = new HashSet<Class<?>>();
    types.add( IAnalyzer.class );

    Set<IDocumentAnalyzer> documentAnalyzers = docController.getAnalyzers( types );
    assertNull( documentAnalyzers );
  }

  @Test
  public void testGetDocumentAnalyzersForType() {
    Set<IDocumentAnalyzer> analyzers = docController.getDocumentAnalyzers( "dummy" );
    assertEquals( 2, analyzers.size() );

    analyzers = docController.getDocumentAnalyzers( "test" );
    assertEquals( 1, analyzers.size() );

    // null type should return everything
    analyzers = docController.getDocumentAnalyzers( null );
    assertEquals( this.analyzers.size(), analyzers.size() );
  }

  @Test
  public void testOnEvent() throws MetaverseAnalyzerException {
    when( mockEvent.getDocument() ).thenReturn( mockDoc );
    when( mockDoc.getType() ).thenReturn( "dummy" );
    when( mockDescriptor.getType()).thenReturn( "dummy" );

    docController.onEvent( mockEvent );

    // timeout to give our asynchronous analyzers a chance to be called
    verify( dummyAnalyzer, timeout( 100 ).times( 1 ) ).analyze( mockDescriptor, mockDoc );
    verify( testAndDummyAnalyzer, timeout( 100 ).times( 1 ) ).analyze( mockDescriptor, mockDoc );
  }

  @Test
  public void testOnEvent_notAllAnalyzersFire() throws MetaverseAnalyzerException {
    when( mockEvent.getDocument() ).thenReturn( mockDoc );
    when( mockDoc.getType() ).thenReturn( "test" );

    docController.onEvent( mockEvent );

    // timeout to give our asynchronous analyzers a chance to be called
    verify( dummyAnalyzer, timeout( 100 ).never() ).analyze( mockDescriptor, mockDoc );
    verify( testAndDummyAnalyzer, timeout( 100 ).times( 1 ) ).analyze( mockDescriptor, mockDoc );
  }

  @Test
  public void testOnEvent_noSupportingAnalyzers() throws MetaverseAnalyzerException {
    when( mockEvent.getDocument() ).thenReturn( mockDoc );
    when( mockDoc.getType() ).thenReturn( "notSupported" );

    docController.onEvent( mockEvent );

    // make sure noe of the analyzers are fired for this un supported type
    verify( dummyAnalyzer, timeout( 100 ).never() ).analyze( mockDescriptor, mockDoc );
    verify( anotherAnalyzer, timeout( 100 ).never() ).analyze( mockDescriptor, mockDoc );
    verify( testAndDummyAnalyzer, timeout( 100 ).never() ).analyze( mockDescriptor, mockDoc );
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

}

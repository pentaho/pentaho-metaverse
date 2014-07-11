package com.pentaho.metaverse.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.metaverse.IDocumentAnalyzer;
import org.pentaho.platform.api.metaverse.IDocumentEvent;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentControllerTest {

  private DocumentController docController;
  private Set<IDocumentAnalyzer> analyzers;

  @Mock private IDocumentAnalyzer dummyAnalyzer;
  @Mock private IDocumentAnalyzer testAndDummyAnalyzer;
  @Mock private IDocumentAnalyzer anotherAnalyzer;
  @Mock private IMetaverseBuilder mockBuilder;
  @Mock private IMetaverseNode mockNode;
  @Mock private IMetaverseLink mockLink;
  @Mock private IDocumentEvent mockEvent;
  @Mock private IMetaverseDocument mockDoc;

  @Before
  public void setup() {
    docController = new DocumentController( );

    IMetaverseBuilder builder = mock( IMetaverseBuilder.class );
    docController.setMetaverseBuilder( builder );
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
    when( anotherAnalyzer.getSupportedTypes() ).thenReturn( supportedTypes3 );

    analyzers.add( dummyAnalyzer );
    analyzers.add( testAndDummyAnalyzer );
    analyzers.add( anotherAnalyzer );

    docController.setDocumentAnalyzers( analyzers );
  }

  @Test
  public void testGetDocumentAnalyzers() {
    Set<IDocumentAnalyzer> analyzers = docController.getDocumentAnalyzers();
    assertEquals( this.analyzers.size(), analyzers.size() );
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
  public void testOnEvent() {
    when( mockEvent.getDocument() ).thenReturn( mockDoc );
    when( mockDoc.getType() ).thenReturn( "dummy" );

    docController.onEvent( mockEvent );

    // timeout to give our asynchronous analyzers a chance to be called
    verify( dummyAnalyzer, timeout(100).times( 1 ) ).analyze( mockDoc );
    verify( testAndDummyAnalyzer, timeout(100).times( 1 ) ).analyze( mockDoc );
  }

  @Test
  public void testBuilderPassthroughCalls() {
    docController = new DocumentController( mockBuilder );

    docController.addNode( mockNode );
    docController.addLink( mockLink );
    docController.updateNode( mockNode );
    docController.updateLink( mockLink );
    docController.deleteLink( mockLink );
    docController.deleteNode( mockNode );

    verify( mockBuilder, times( 1 ) ).addNode( mockNode );
    verify( mockBuilder, times( 1 ) ).addLink( mockLink );
    verify( mockBuilder, times( 1 ) ).updateNode( mockNode );
    verify( mockBuilder, times( 1 ) ).updateLink( mockLink );
    verify( mockBuilder, times( 1 ) ).deleteNode( mockNode );
    verify( mockBuilder, times( 1 ) ).deleteLink( mockLink );

  }

}

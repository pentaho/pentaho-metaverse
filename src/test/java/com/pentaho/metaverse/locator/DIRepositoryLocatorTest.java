package com.pentaho.metaverse.locator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.api.metaverse.IDocumentEvent;
import org.pentaho.platform.api.metaverse.IDocumentListener;

import com.pentaho.metaverse.impl.MetaverseDocument;

@SuppressWarnings( { "all" } )

public class DIRepositoryLocatorTest implements IDocumentListener {

  private List<IDocumentEvent> events;
  
  public DIRepositoryLocatorTest() throws Exception {

    
    
  }

  @Before
  public void init() {
    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  
  @Test
  public void testStartIndexer() throws Exception {

    DIRepositoryLocator locator = new DIRepositoryLocator();
    
    locator.addDocumentListener( this );
    locator.setRepository( new TestDiRepository() );
    locator.setUnifiedRepository( new TestDiUnifiedRepository() );

    assertNotNull("Indexer type is null", locator.getIndexerType() );
    events = new ArrayList<IDocumentEvent>();
    locator.startScan();
    Thread.sleep(3000);

    assertEquals( "Event count is wrong", 7, events.size() );

    for( IDocumentEvent event : events ) {
      System.out.println(event.getDocument().getID());
      assertNotNull( event.getDocument() );
      MetaverseDocument document = (MetaverseDocument) event.getDocument();
      assertNotNull( document.getContent() );
    }
    
  }

  @Override
  public void onEvent( IDocumentEvent event ) {
    events.add( event );
  }

}

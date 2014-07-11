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

/**
 * Test class for the DIRepositoryLocator
 * @author jdixon
 *
 */
@SuppressWarnings( { "all" } )
public class DIRepositoryLocatorTest implements IDocumentListener {

  private List<IDocumentEvent> events;

  /**
   * Initializes the kettle system
   */
  @Before
  public void init() {
    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * Runs the locator and checks the results
   * @throws Exception When bad things happen
   */
  @Test
  public void testStartLocator() throws Exception {

    DIRepositoryLocator locator = new DIRepositoryLocator();

    locator.addDocumentListener( this );
    locator.setRepository( new TestDiRepository() );
    locator.setUnifiedRepository( new TestDiUnifiedRepository() );

    assertNotNull("Indexer type is null", locator.getIndexerType() );
    events = new ArrayList<IDocumentEvent>();
    locator.startScan();
    Thread.sleep( 3000 );

    assertEquals( "Event count is wrong", 7, events.size() );

    for ( IDocumentEvent event : events ) {
      System.out.println( event.getDocument().getID() );
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

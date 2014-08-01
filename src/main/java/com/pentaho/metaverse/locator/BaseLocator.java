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

import java.util.ArrayList;
import java.util.List;

import com.pentaho.metaverse.impl.MetaverseCompletionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.metaverse.IDocumentEvent;
import org.pentaho.platform.api.metaverse.IDocumentListener;
import org.pentaho.platform.api.metaverse.IDocumentLocator;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.DictionaryHelper;

import java.util.concurrent.Future;

/**
 * Base implementation for all @see org.pentaho.platform.api.metaverse.IDocumentLocator implementations
 * @author jdixon
 *
 */
public abstract class BaseLocator<T> implements IDocumentLocator {

  private static final long serialVersionUID = 693428630030858039L;

  private static final String LOCATOR_ID_PREFIX = "locator_";

  private static final int POLLING_INTERVAL = 100;

  private static final Logger log = LoggerFactory.getLogger( BaseLocator.class );


  /**
   * The node in the metaverse that represents the root of this located domain/namespace
   */
  protected IMetaverseNode locatorNode;

  /**
   * The user session to use for the locator to use
   */
  protected IPentahoSession session;

  /**
   * The metaverse builder for adding this locator and its documents to
   */
  protected IMetaverseBuilder metaverseBuilder;

  /**
   * The unique id of the locator
   */
  protected String id = "";

  /**
   * The unique type of the locator
   */
  protected String locatorType;

  private List<IDocumentListener> listeners = new ArrayList<IDocumentListener>();

  protected LocatorRunner runner;

  protected MetaverseCompletionService completionService = MetaverseCompletionService.getInstance();

  protected Future<String> futureTask;

  /**
   * Constructor for the abstract super class
   */
  public BaseLocator() {
    DictionaryHelper.registerEntityType( DictionaryConst.NODE_TYPE_LOCATOR );
  }

  /**
   * A method that returns the payload (object or XML) for a document
   * @param locatedItem item to harvest; ie., a file
   * @return The object or XML payload
   * @throws Exception When the document contents cannot be retrieved
   */
  protected abstract Object getContents( T locatedItem ) throws Exception;

  /**
   * Constructor that takes in a List of IDocumentListeners
   * @param documentListeners the List of listeners
   */
  public BaseLocator( List<IDocumentListener> documentListeners ) {
    this.listeners = documentListeners;
  }

  @Override
  public void addDocumentListener( IDocumentListener listener ) {
    listeners.add( listener );
  }

  @Override
  public void notifyListeners( IDocumentEvent event ) {
    for ( IDocumentListener listener : listeners ) {
      listener.onEvent( event );
    }
  }

  @Override
  public void removeDocumentListener( IDocumentListener listener ) {
    listeners.remove( listener );
  }

  public String getRepositoryId() {
    return id;
  }

  public void setRepositoryId( String id ) {
    this.id = id;
  }

  public String getLocatorType() {
    return locatorType;
  }

  public void setLocatorType( String locatorType ) {
    this.locatorType = locatorType;
  }

  public IMetaverseBuilder getMetaverseBuilder() {
    return metaverseBuilder;
  }

  public void setMetaverseBuilder( IMetaverseBuilder metaverseBuilder ) {
    this.metaverseBuilder = metaverseBuilder;
  }

  public IMetaverseNode getLocatorNode() {

    if ( locatorNode == null ) {

      locatorNode = metaverseBuilder.getMetaverseObjectFactory().createNodeObject(
          LOCATOR_ID_PREFIX + id,
          id,
          DictionaryConst.NODE_TYPE_LOCATOR );

    }

    return locatorNode;
  }

  public void setLocatorNode( IMetaverseNode locatorNode ) {
    this.locatorNode = locatorNode;
  }

  /**
   * TODO Change this once ID generation is refactored
   */
  protected String getId( String... tokens ) {
    return getLocatorType() + "." + getRepositoryId() + "." + tokens[0];
  }

  @Override
  public void stopScan() {
    if ( futureTask == null || futureTask.isDone() || futureTask.isCancelled() ) {
      // already stopped
      return;
    }

    System.out.println( "RepositoryLocator stopScan" );

    runner.stop();
    futureTask.cancel( false );
    futureTask = null;
    runner = null;
  }


  protected void startScan( LocatorRunner locatorRunner ) {

    if ( futureTask != null && !futureTask.isDone() ) {
      //TODO      throw new Exception("Locator is already scanning");
      return;
    }

    IMetaverseNode node = getLocatorNode();
    metaverseBuilder.addNode( node );

    runner = locatorRunner;
    runner.setLocator(  this );

    futureTask = completionService.submit( runner, node.getStringID() );
  }

}

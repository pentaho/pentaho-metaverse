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

import com.pentaho.metaverse.api.INamespaceFactory;
import com.pentaho.metaverse.impl.MetaverseCompletionService;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.platform.api.metaverse.IDocumentListener;
import org.pentaho.platform.api.metaverse.IDocumentEvent;
import org.pentaho.platform.api.metaverse.IDocumentLocator;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseLocatorException;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.pentaho.platform.api.engine.IPentahoSession;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.DictionaryHelper;

import java.util.concurrent.Future;

/**
 * Base implementation for all @see org.pentaho.platform.api.metaverse.IDocumentLocator implementations
 *
 * @author jdixon
 *
 * @param <T> The type of this locator
 */
public abstract class BaseLocator<T> implements IDocumentLocator {

  private static final long serialVersionUID = 693428630030858039L;

  private static final Logger LOG = LoggerFactory.getLogger( BaseLocator.class );

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

  /**
   * The runner for this locator so that the document location is asynchronous
   */
  protected LocatorRunner<T> runner;

  /**
   * The completion service to use. This tracks the execution of a locator scan.
   */
  protected MetaverseCompletionService completionService = MetaverseCompletionService.getInstance();

  /**
   * The result of the scan of this locator
   */
  protected Future<String> futureTask;

  private List<IDocumentListener> listeners = new ArrayList<IDocumentListener>();

  /**
   * Constructor for the abstract super class
   */
  public BaseLocator() {
    DictionaryHelper.registerEntityType( DictionaryConst.NODE_TYPE_LOCATOR );
  }

  /**
   * Constructor that takes in a List of IDocumentListeners
   *
   * @param documentListeners the List of listeners
   */
  public BaseLocator( List<IDocumentListener> documentListeners ) {
    this.listeners = documentListeners;
  }

  /**
   * A method that returns the payload (object or XML) for a document
   *
   * @param locatedItem item to harvest; ie., a file
   * @return The object or XML payload
   * @throws Exception When the document contents cannot be retrieved
   */
  protected abstract Object getContents( T locatedItem ) throws Exception;

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

  /**
   * Returns the locator node for this locator. The locator node is the node in the metaverse
   * that represents this locator. It is used to create a link from this locator to the documents
   * that are found by/within it.
   * 
   * @return The locator node in the metaverse
   */
  public IMetaverseNode getLocatorNode() {

    if ( locatorNode == null ) {

      locatorNode = metaverseBuilder.getMetaverseObjectFactory().createNodeObject(
          getNamespace().getNamespaceId(),
          getRepositoryId(),
          DictionaryConst.NODE_TYPE_LOCATOR );

    }

    return locatorNode;
  }

  public void setLocatorNode( IMetaverseNode locatorNode ) {
    this.locatorNode = locatorNode;
  }

  protected INamespace getNamespace() {

    return getNamespaceFactory().createNameSpace( null,
        getLocatorType().concat( DictionaryHelper.SEPARATOR.concat( getRepositoryId() ) ),
        DictionaryConst.NODE_TYPE_LOCATOR );
  }

  @Override
  public void stopScan() {
    if ( futureTask == null || futureTask.isDone() || futureTask.isCancelled() ) {
      // already stopped
      return;
    }

    LOG.debug( "Locator type {}: stopScan()", getLocatorType() );

    runner.stop();
    futureTask.cancel( false );
    futureTask = null;
    runner = null;
  }

  /**
   * Starts a full scan by this locator.
   *
   * @exception org.pentaho.platform.api.metaverse.MetaverseLocatorException
   * @param locatorRunner The locator runner to use
   */
  protected void startScan( LocatorRunner<T> locatorRunner ) throws MetaverseLocatorException {

    if ( futureTask != null && !futureTask.isDone() ) {
      throw new MetaverseLocatorException( Messages.getString( "ERROR.BaseLocator.ScanAlreadyExecuting" ) );
    }

    IMetaverseNode node = getLocatorNode();
    metaverseBuilder.addNode( node );

    runner = locatorRunner;
    runner.setLocator( this );

    LOG.debug( "Locator type {}: startScan()", getLocatorType() );

    futureTask = completionService.submit( runner, node.getStringID() );
  }

  public INamespaceFactory getNamespaceFactory() {
    return PentahoSystem.get( INamespaceFactory.class );
  }

}

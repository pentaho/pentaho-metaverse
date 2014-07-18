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

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.metaverse.IDocumentEvent;
import org.pentaho.platform.api.metaverse.IDocumentListener;
import org.pentaho.platform.api.metaverse.IDocumentLocator;
import org.pentaho.platform.engine.core.system.PentahoBase;

import com.pentaho.dictionary.IIdGenerator;

/**
 * Base implementation for all @see org.pentaho.platform.api.metaverse.IDocumentLocator implementations
 * @author jdixon
 *
 */
public abstract class BaseLocator extends PentahoBase implements IDocumentLocator, IIdGenerator {

  private static final long serialVersionUID = 693428630030858039L;

  /**
   * The user session to use for the locator to use
   */
  protected IPentahoSession session;

  /**
   * The unique id of the locator
   */
  protected String id = "";

  /**
   * The unique type of the locator
   */
  protected String indexerType;

  private List<IDocumentListener> listeners = new ArrayList<IDocumentListener>();

  /**
   * Constructor for the abstract super class
   */
  public BaseLocator() {
  }

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

  public String getIndexerType() {
    return indexerType;
  }

  public void setIndexerType( String indexerType ) {
    this.indexerType = indexerType;
  }

}

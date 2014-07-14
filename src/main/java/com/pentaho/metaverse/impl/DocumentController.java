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

package com.pentaho.metaverse.impl;

import com.pentaho.metaverse.api.IDocumentAnalyzerProvider;
import org.pentaho.platform.api.metaverse.IDocumentAnalyzer;
import org.pentaho.platform.api.metaverse.IDocumentEvent;
import org.pentaho.platform.api.metaverse.IDocumentListener;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Coordinates passing IDocumentEvent's to the appropriate IDocumentAnalyzer's
 */
public class DocumentController implements IDocumentListener, IMetaverseBuilder, IDocumentAnalyzerProvider {

  /** The metaverse builder. */
  private IMetaverseBuilder metaverseBuilder;

  /** The analyzers. */
  private Set<IDocumentAnalyzer> analyzers = new HashSet<IDocumentAnalyzer>();

  /** The analyzer type map. */
  private Map<String, HashSet<IDocumentAnalyzer>> analyzerTypeMap = new HashMap<String, HashSet<IDocumentAnalyzer>>();

  private final ExecutorService pool = Executors.newFixedThreadPool( 5 );

  /**
   * Empty constructor
   */
  public DocumentController() {
  }

  /**
   * Constructor that takes in an IMetaverseBuilder
   * @param metaverseBuilder builder to delegate building calls to
   */
  public DocumentController( IMetaverseBuilder metaverseBuilder ) {
    this.metaverseBuilder = metaverseBuilder;
  }

  /**
   * Gets the metaverse builder.
   * 
   * @return the metaverse builder
   */
  protected IMetaverseBuilder getMetaverseBuilder() {
    return metaverseBuilder;
  }

  /**
   * Sets the metaverse builder.
   * 
   * @param metaverseBuilder
   *          the new metaverse builder
   */
  public void setMetaverseBuilder( IMetaverseBuilder metaverseBuilder ) {
    this.metaverseBuilder = metaverseBuilder;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.pentaho.metaverse.api.IDocumentAnalyzerProvider#getDocumentAnalyzers()
   */
  @Override
  public Set<IDocumentAnalyzer> getDocumentAnalyzers() {
    return analyzers;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.pentaho.metaverse.api.IDocumentAnalyzerProvider#getDocumentAnalyzers(java.lang.String)
   */
  @Override
  public Set<IDocumentAnalyzer> getDocumentAnalyzers( String type ) {
    if ( type == null ) {
      return getDocumentAnalyzers();
    }

    return analyzerTypeMap.get( type );
  }

  /**
   * Set the analyzers that are available in the system
   * @param analyzers the complete Set of IDocumentAnalyzers
   */
  public void setDocumentAnalyzers( Set<IDocumentAnalyzer> analyzers ) {
    this.analyzers = analyzers;
    // reset the analyzer type map
    loadAnalyzerTypeMap();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IDocumentListener#onEvent(org.pentaho.platform.api.metaverse.IDocumentEvent)
   */
  @Override
  public void onEvent( IDocumentEvent event ) {
    Set<IDocumentAnalyzer> matchingAnalyzers = getDocumentAnalyzers( event.getDocument().getType() );
    for ( IDocumentAnalyzer analyzer : matchingAnalyzers ) {
      Set<String> types = analyzer.getSupportedTypes();
      if ( types != null && types.contains( event.getDocument().getType() ) ) {
        fireDocumentEvent( event, analyzer );
      }
    }
  }

  /**
   * Loads up a Map of document types to supporting IDocumentAnalyzer(s)
   */
  protected void loadAnalyzerTypeMap() {
    analyzerTypeMap = new HashMap<String, HashSet<IDocumentAnalyzer>>( );
    for ( IDocumentAnalyzer analyzer : analyzers ) {
      Set<String> types = analyzer.getSupportedTypes();
      analyzer.setMetaverseBuilder( this );

      if ( types != null ) {
        for ( String type : types ) {
          HashSet<IDocumentAnalyzer> analyzerSet = null;
          if ( analyzerTypeMap.containsKey( type ) ) {
            // we already have someone that handles this type, add to the Set
            analyzerSet = analyzerTypeMap.get( type );
          } else {
            // no one else (yet) handles this type, add it in
            analyzerSet = new HashSet<IDocumentAnalyzer>();
          }
          analyzerSet.add( analyzer );
          analyzerTypeMap.put( type, analyzerSet );
        }
      }
    }
  }

  /**
   * Fires a IDocumentEvent to an IDocumentAnalyzer in a separate Thread
   * @param event IDocumentEvent to fire
   * @param analyzer IDocumentAnalyzer to use for the Document that needs processed
   * @return Future object
   */
  protected Future<?> fireDocumentEvent( final IDocumentEvent event, final IDocumentAnalyzer analyzer ) {

    Runnable analyzerRunner = new Runnable() {
      @Override public void run() {
        analyzer.analyze( event.getDocument() );
      }
    };

    return pool.submit( analyzerRunner, null );

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#addNode(org.pentaho.platform.api.metaverse.IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder addNode( IMetaverseNode iMetaverseNode ) {
    return metaverseBuilder.addNode( iMetaverseNode );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#addLink(org.pentaho.platform.api.metaverse.IMetaverseLink)
   */
  @Override
  public IMetaverseBuilder addLink( IMetaverseLink iMetaverseLink ) {
    return metaverseBuilder.addLink( iMetaverseLink );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#deleteNode(org.pentaho.platform.api.metaverse.IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder deleteNode( IMetaverseNode iMetaverseNode ) {
    return metaverseBuilder.deleteNode( iMetaverseNode );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#deleteLink(org.pentaho.platform.api.metaverse.IMetaverseLink)
   */
  @Override
  public IMetaverseBuilder deleteLink( IMetaverseLink iMetaverseLink ) {
    return metaverseBuilder.deleteLink( iMetaverseLink );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#updateNode(org.pentaho.platform.api.metaverse.IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder updateNode( IMetaverseNode iMetaverseNode ) {
    return metaverseBuilder.updateNode( iMetaverseNode );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#updateLink(org.pentaho.platform.api.metaverse.IMetaverseLink)
   */
  @Override
  public IMetaverseBuilder updateLink( IMetaverseLink iMetaverseLink ) {
    return metaverseBuilder.updateLink( iMetaverseLink );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#addLink(org.pentaho.platform.api.metaverse.IMetaverseNode,
   * java.lang.String, org.pentaho.platform.api.metaverse.IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder addLink( IMetaverseNode fromNode, String label, IMetaverseNode toNode ) {
    return metaverseBuilder.addLink( fromNode, label, toNode );
  }

}

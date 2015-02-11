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

import com.pentaho.metaverse.api.IDocumentController;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.platform.api.metaverse.IDocumentAnalyzer;
import org.pentaho.platform.api.metaverse.IDocumentEvent;
import org.pentaho.platform.api.metaverse.IDocumentListener;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.IRequiresMetaverseBuilder;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Coordinates passing IDocumentEvent's to the appropriate IDocumentAnalyzer's
 */
public class DocumentController implements IDocumentController, IDocumentListener, IRequiresMetaverseBuilder {

  /**
   * The metaverse builder.
   */
  private IMetaverseBuilder metaverseBuilder;

  /**
   * the metaverse object factory
   */
  private IMetaverseObjectFactory metaverseObjectFactory;

  /**
   * The analyzers.
   */
  private Set<IDocumentAnalyzer> documentAnalyzers = new HashSet<IDocumentAnalyzer>();

  /**
   * The analyzer type map.
   */
  private Map<String, HashSet<IDocumentAnalyzer>> analyzerTypeMap = new HashMap<String, HashSet<IDocumentAnalyzer>>();

  private static final Logger log = LoggerFactory.getLogger( DocumentController.class );

  /**
   * Empty constructor
   */
  public DocumentController() {
  }

  /**
   * Constructor that takes in an IMetaverseBuilder
   *
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
  @Override
  public IMetaverseBuilder getMetaverseBuilder() {
    return metaverseBuilder;
  }

  /**
   * Sets the metaverse builder.
   *
   * @param metaverseBuilder the new metaverse builder
   */
  @Override
  public void setMetaverseBuilder( IMetaverseBuilder metaverseBuilder ) {
    this.metaverseBuilder = metaverseBuilder;
  }

  /**
   * Gets the metaverse object factory
   *
   * @return the metaverse object factory
   */
  public IMetaverseObjectFactory getMetaverseObjectFactory() {
    return ( metaverseBuilder == null ) ? null : metaverseBuilder.getMetaverseObjectFactory();
  }

  /**
   * Sets the metaverse object factory
   *
   * @param metaverseObjectFactory the new metaverse object factory
   */
  public void setMetaverseObjectFactory( IMetaverseObjectFactory metaverseObjectFactory ) {
    if ( metaverseBuilder != null ) {
      metaverseBuilder.setMetaverseObjectFactory( metaverseObjectFactory );
    }
  }

  /*
     * (non-Javadoc)
     *
     * @see com.pentaho.metaverse.api.IDocumentAnalyzerProvider#getDocumentAnalyzers()
     */
  @Override
  public Set<IDocumentAnalyzer> getAnalyzers() {
    return documentAnalyzers;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.pentaho.metaverse.api.IDocumentAnalyzerProvider#getDocumentAnalyzers(java.lang.String)
   */
  @Override
  public Set<IDocumentAnalyzer> getDocumentAnalyzers( String type ) {
    if ( type == null ) {
      return getAnalyzers();
    }

    return analyzerTypeMap.get( type );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.pentaho.metaverse.api.IAnalyzerProvider#getAnalyzers(java.util.Set)
   */
  @Override
  public Set<IDocumentAnalyzer> getAnalyzers( Set<Class<?>> types ) {
    if ( types == null || ( types.size() == 1 && types.contains( IDocumentAnalyzer.class ) ) ) {
      return getAnalyzers();
    }
    return null;
  }

  /**
   * Set the analyzers that are available in the system
   *
   * @param documentAnalyzers the complete Set of IDocumentAnalyzers
   */
  @Override
  public void setDocumentAnalyzers( Set<IDocumentAnalyzer> documentAnalyzers ) {
    this.documentAnalyzers = documentAnalyzers;
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
    Set<IDocumentAnalyzer> matchingAnalyzers = getDocumentAnalyzers( event.getDocument().getExtension() );
    if ( matchingAnalyzers != null ) {
      for ( IDocumentAnalyzer analyzer : matchingAnalyzers ) {
        fireDocumentEvent( event, analyzer );
      }
    } else {
      log.warn( Messages.getString( "WARNING.NoMatchingDocumentAnalyzerFound", event.getDocument().getExtension() ) );
    }
  }

  /**
   * Loads up a Map of document types to supporting IDocumentAnalyzer(s)
   */
  protected void loadAnalyzerTypeMap() {
    analyzerTypeMap = new HashMap<String, HashSet<IDocumentAnalyzer>>();
    for ( IDocumentAnalyzer analyzer : documentAnalyzers ) {
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
   *
   * @param event    IDocumentEvent to fire
   * @param analyzer IDocumentAnalyzer to use for the Document that needs processed
   * @return Future object
   */
  protected Future<?> fireDocumentEvent( final IDocumentEvent event, final IDocumentAnalyzer analyzer ) {
    Runnable analyzerRunner = new Runnable() {
      @Override
      public void run() {
        try {

          analyzer.analyze(
            new MetaverseComponentDescriptor(
              event.getDocument().getName(),
              event.getDocument().getType(),
              event.getDocument() ),
            event.getDocument()
          );
        } catch ( MetaverseAnalyzerException mae ) {
          log.error( Messages.getString( "ERROR.AnalyzingDocument", event.getDocument().getStringID() ), mae );
        }
      }
    };

    return MetaverseCompletionService.getInstance().submit( analyzerRunner, event.getDocument().getStringID() );
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
   * @see org.pentaho.platform.api.metaverse.IMetaverseBuilder#
   * updateLinkLabel(org.pentaho.platform.api.metaverse.IMetaverseLink , java.lang.String)
   */
  @Override
  public IMetaverseBuilder updateLinkLabel( IMetaverseLink iMetaverseLink, String newLabel ) {
    return metaverseBuilder.updateLinkLabel( iMetaverseLink, newLabel );
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

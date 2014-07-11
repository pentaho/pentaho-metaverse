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

/**
 * Coordinates passing IDocumentEvent's to the appropriate IDocumentAnalyzer's
 */
public class DocumentController implements IDocumentListener, IMetaverseBuilder, IDocumentAnalyzerProvider {

  private IMetaverseBuilder metaverseBuilder = null;
  private Set<IDocumentAnalyzer> analyzers = new HashSet<IDocumentAnalyzer>();
  private Map<String, HashSet<IDocumentAnalyzer>> analyzerTypeMap = new HashMap<String, HashSet<IDocumentAnalyzer>>( );

  public DocumentController() {
  }

  public DocumentController( IMetaverseBuilder metaverseBuilder ) {
    this.metaverseBuilder = metaverseBuilder;
  }

  public IMetaverseBuilder getMetaverseBuilder() {
    return metaverseBuilder;
  }

  public void setMetaverseBuilder( IMetaverseBuilder metaverseBuilder ) {
    this.metaverseBuilder = metaverseBuilder;
  }

  @Override
  public Set<IDocumentAnalyzer> getDocumentAnalyzers() {
    return analyzers;
  }

  @Override
  public Set<IDocumentAnalyzer> getDocumentAnalyzers( String type ) {
    if ( type == null ) {
      return getDocumentAnalyzers();
    }

    if ( analyzerTypeMap.size() == 0 ) {
      loadAnalyzerTypeMap();
    }
    return analyzerTypeMap.get( type );
  }

  public void setDocumentAnalyzers( Set<IDocumentAnalyzer> analyzers ) {
    this.analyzers = analyzers;
  }

  @Override
  public void onEvent( IDocumentEvent event ) {
    if ( analyzerTypeMap.size() == 0 ) {
      loadAnalyzerTypeMap();
    }

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
    for ( IDocumentAnalyzer analyzer : analyzers ) {
      Set<String> types = analyzer.getSupportedTypes();

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
   * @param event
   * @param analyzer
   */
  protected void fireDocumentEvent( final IDocumentEvent event, final IDocumentAnalyzer analyzer ) {
    analyzer.setMetaverseBuilder( getMetaverseBuilder() );

    Runnable analyerRunner = new Runnable() {
      @Override public void run() {
        analyzer.analyze( event.getDocument() );
      }
    };

    Thread t = new Thread( analyerRunner );
    t.start();

  }

  @Override
  public IMetaverseBuilder addNode( IMetaverseNode iMetaverseNode ) {
    return metaverseBuilder.addNode( iMetaverseNode );
  }

  @Override
  public IMetaverseBuilder addLink( IMetaverseLink iMetaverseLink ) {
    return metaverseBuilder.addLink( iMetaverseLink );
  }

  @Override
  public IMetaverseBuilder deleteNode( IMetaverseNode iMetaverseNode ) {
    return metaverseBuilder.deleteNode( iMetaverseNode );
  }

  @Override
  public IMetaverseBuilder deleteLink( IMetaverseLink iMetaverseLink ) {
    return metaverseBuilder.deleteLink( iMetaverseLink );
  }

  @Override
  public IMetaverseBuilder updateNode( IMetaverseNode iMetaverseNode ) {
    return metaverseBuilder.updateNode( iMetaverseNode );
  }

  @Override
  public IMetaverseBuilder updateLink( IMetaverseLink iMetaverseLink ) {
    return metaverseBuilder.updateLink( iMetaverseLink );
  }

}

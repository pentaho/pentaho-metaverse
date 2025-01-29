/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.impl;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.pentaho.metaverse.api.IClonableDocumentAnalyzer;
import org.pentaho.metaverse.api.IDocumentAnalyzer;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IDocumentEvent;
import org.pentaho.metaverse.api.IDocumentListener;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseLink;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.IRequiresMetaverseBuilder;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Coordinates passing IDocumentEvent's to the appropriate IDocumentAnalyzer's
 */
public class DocumentController implements IDocumentController, IDocumentListener, IRequiresMetaverseBuilder {

  private static DocumentController instance;

  public static DocumentController getInstance() {
    if ( null == instance ) {
      instance = new DocumentController();
    }
    return instance;
  }

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
  private List<IDocumentAnalyzer> documentAnalyzers = new ArrayList<IDocumentAnalyzer>();

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
    return metaverseObjectFactory != null ? metaverseObjectFactory
      : ( metaverseBuilder == null ) ? null : metaverseBuilder.getMetaverseObjectFactory();
  }

  /**
   * Sets the metaverse object factory
   *
   * @param metaverseObjectFactory the new metaverse object factory
   */
  public void setMetaverseObjectFactory( IMetaverseObjectFactory metaverseObjectFactory ) {
    this.metaverseObjectFactory = metaverseObjectFactory;
    if ( metaverseBuilder != null ) {
      metaverseBuilder.setMetaverseObjectFactory( metaverseObjectFactory );
    }
  }

  /**
   * Returns the underlying graph associated with this builder
   *
   * @return the backing Graph object
   */
  @Override
  public Graph getGraph() {
    if ( metaverseBuilder != null ) {
      return metaverseBuilder.getGraph();
    }
    return null;
  }

  /**
   * Sets the underlying graph for this builder
   *
   * @param graph the graph to set for the builder
   */
  @Override
  public void setGraph( Graph graph ) {
    if ( metaverseBuilder != null ) {
      metaverseBuilder.setGraph( graph );
    }
  }

  /*
     * (non-Javadoc)
     *
     * @see IDocumentAnalyzerProvider#getDocumentAnalyzers()
     */
  @Override
  public List<IDocumentAnalyzer> getAnalyzers() {
    return documentAnalyzers;
  }

  /*
   * (non-Javadoc)
   * 
   * @see IDocumentAnalyzerProvider#getDocumentAnalyzers(java.lang.String)
   */
  @Override
  public List<IDocumentAnalyzer> getDocumentAnalyzers( String type ) {
    if ( type == null ) {
      return getAnalyzers();
    }
    HashSet<IDocumentAnalyzer> documentAnalyzerHashSet = analyzerTypeMap.get( type );
    List<IDocumentAnalyzer> docAnalyzers = null;
    if ( documentAnalyzerHashSet != null ) {
      docAnalyzers = new ArrayList<IDocumentAnalyzer>( documentAnalyzerHashSet );
    }
    return docAnalyzers;
  }

  /*
   * (non-Javadoc)
   * 
   * @see IAnalyzerProvider#getAnalyzers(java.util.Set)
   */
  @Override
  public List<IDocumentAnalyzer> getAnalyzers( Collection<Class<?>> types ) {
    if ( types == null || ( types.size() == 1 && types.contains( IDocumentAnalyzer.class ) ) ) {
      return getAnalyzers();
    }
    return null;
  }

  /**
   * Set the analyzers that are available in the system
   *
   * @param documentAnalyzers the complete List of IDocumentAnalyzers
   */
  @Override
  public void setDocumentAnalyzers( List<IDocumentAnalyzer> documentAnalyzers ) {
    if ( documentAnalyzers == null ) {
      this.documentAnalyzers = null;
    } else {
      if ( this.documentAnalyzers == null ) {
        this.documentAnalyzers = new ArrayList();
      }
      this.documentAnalyzers.addAll( documentAnalyzers );
      // reset the analyzer type map
      loadAnalyzerTypeMap();
    }
  }

  public void setClonableDocumentAnalyzers( List<IDocumentAnalyzer> documentAnalyzers ) {
    setDocumentAnalyzers( documentAnalyzers );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * IDocumentListener#onEvent(IDocumentEvent)
   */
  @Override
  public void onEvent( IDocumentEvent event ) {
    List<IDocumentAnalyzer> matchingAnalyzers = getDocumentAnalyzers( event.getDocument().getExtension() );
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
      addAnalyzer( analyzer );
    }
  }

  public void addClonableAnalyzer( final IClonableDocumentAnalyzer analyzer ) {
    addAnalyzer( analyzer );
  }

  @Override
  public void addAnalyzer( IDocumentAnalyzer analyzer ) {
    if ( !documentAnalyzers.contains( analyzer ) ) {
      documentAnalyzers.add( analyzer );
    }
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

  public void removeClonableAnalyzer( final IClonableDocumentAnalyzer analyzer ) {
    removeAnalyzer( analyzer );
  }

  @Override
  public void removeAnalyzer( IDocumentAnalyzer analyzer ) {
    if ( documentAnalyzers.contains( analyzer ) ) {
      try {
        documentAnalyzers.remove( analyzer );
      } catch ( UnsupportedOperationException uoe ) {
        // This comes from Blueprint for managed containers (which are read-only). Nothing to do in this case
      }
    }
    if ( analyzer != null ) {
      Set<String> types = analyzer.getSupportedTypes();
      analyzer.setMetaverseBuilder( this );
      if ( types != null ) {
        for ( String type : types ) {
          HashSet<IDocumentAnalyzer> analyzerSet = null;
          if ( analyzerTypeMap.containsKey( type ) ) {
            // we have someone that handles this type, remove it from the set
            analyzerSet = analyzerTypeMap.get( type );
            analyzerSet.remove( analyzer );
            if ( analyzerSet.size() == 0 ) {
              analyzerTypeMap.remove( type );
            }
          }
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
   * IMetaverseBuilder#addNode(IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder addNode( IMetaverseNode iMetaverseNode ) {
    return metaverseBuilder.addNode( iMetaverseNode );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * IMetaverseBuilder#addLink(IMetaverseLink)
   */
  @Override
  public IMetaverseBuilder addLink( IMetaverseLink iMetaverseLink ) {
    return metaverseBuilder.addLink( iMetaverseLink );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * IMetaverseBuilder#deleteNode(IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder deleteNode( IMetaverseNode iMetaverseNode ) {
    return metaverseBuilder.deleteNode( iMetaverseNode );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * IMetaverseBuilder#deleteLink(IMetaverseLink)
   */
  @Override
  public IMetaverseBuilder deleteLink( IMetaverseLink iMetaverseLink ) {
    return metaverseBuilder.deleteLink( iMetaverseLink );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * IMetaverseBuilder#updateNode(IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder updateNode( IMetaverseNode iMetaverseNode ) {
    return metaverseBuilder.updateNode( iMetaverseNode );
  }

  /*
   * (non-Javadoc)
   * 
   * @see IMetaverseBuilder#
   * updateLinkLabel(IMetaverseLink , java.lang.String)
   */
  @Override
  public IMetaverseBuilder updateLinkLabel( IMetaverseLink iMetaverseLink, String newLabel ) {
    return metaverseBuilder.updateLinkLabel( iMetaverseLink, newLabel );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * IMetaverseBuilder#addLink(IMetaverseNode,
   * java.lang.String, IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder addLink( IMetaverseNode fromNode, String label, IMetaverseNode toNode ) {
    return metaverseBuilder.addLink( fromNode, label, toNode );
  }

  @Override
  public void addLink( Vertex fromVertex, String label, Vertex toVertex ) {
    metaverseBuilder.addLink( fromVertex, label, toVertex );
  }
}

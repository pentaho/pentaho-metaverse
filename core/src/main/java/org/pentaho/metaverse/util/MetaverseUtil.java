/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.util;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import flexjson.JSONDeserializer;
import org.pentaho.di.core.Const;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.DictionaryHelper;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IClonableDocumentAnalyzer;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentAnalyzer;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operation;
import org.pentaho.metaverse.api.model.Operations;
import org.pentaho.metaverse.graph.LineageGraphCompletionService;
import org.pentaho.metaverse.graph.LineageGraphMap;
import org.pentaho.metaverse.impl.MetaverseBuilder;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.pentaho.metaverse.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;


/**
 * A class containing utility methods for Metaverse / lineage
 */
public class MetaverseUtil {

  private static final Logger log = LoggerFactory.getLogger( MetaverseUtil.class );

  public static final String MESSAGE_PREFIX_NODETYPE = "USER.nodetype.";
  public static final String MESSAGE_PREFIX_LINKTYPE = "USER.linktype.";
  public static final String MESSAGE_PREFIX_CATEGORY = "USER.category.";
  public static final String MESSAGE_FAILED_PREFIX = "!";

  protected static IDocumentController documentController = null;

  public static IDocumentController getDocumentController() {
    if ( documentController != null ) {
      return documentController;
    }
    try {
      documentController = (IDocumentController) MetaverseBeanUtil.getInstance().get( "IDocumentController" );
    } catch ( Exception e ) {
      // Just return null
    }
    return documentController;
  }

  public static void setDocumentController( IDocumentController docController ) {
    documentController = docController;
  }

  public static IDocument createDocument(
    INamespace namespace,
    Object content,
    String id,
    String name,
    String extension,
    String mimeType ) {

    IDocument metaverseDocument = getDocumentController().getMetaverseObjectFactory().createDocumentObject();

    metaverseDocument.setNamespace( namespace );
    metaverseDocument.setContent( content );
    metaverseDocument.setStringID( id );
    metaverseDocument.setName( name );
    metaverseDocument.setExtension( extension );
    metaverseDocument.setMimeType( mimeType );
    metaverseDocument.setProperty( DictionaryConst.PROPERTY_PATH, id );
    metaverseDocument.setProperty( DictionaryConst.PROPERTY_NAMESPACE, namespace.getNamespaceId() );

    return metaverseDocument;
  }

  public static void addLineageGraph( final IDocument document, Graph graph ) throws MetaverseException {

    if ( document == null ) {
      throw new MetaverseException( Messages.getString( "ERROR.Document.IsNull" ) );
    }

    // Find the transformation analyzer(s) and create Futures to analyze the transformation.
    // Right now we expect a single transformation analyzer in the system. If we need to support more,
    // the lineageGraphMap needs to map the TransMeta to a collection of Futures, etc.
    IDocumentController docController = MetaverseUtil.getDocumentController();
    if ( docController != null ) {

      // Create a new builder, setting it on the DocumentController if possible
      IMetaverseBuilder metaverseBuilder = new MetaverseBuilder( graph );

      docController.setMetaverseBuilder( metaverseBuilder );
      List<IDocumentAnalyzer> matchingAnalyzers = docController.getDocumentAnalyzers( "ktr" );

      if ( matchingAnalyzers != null ) {
        for ( IDocumentAnalyzer analyzer : matchingAnalyzers ) {

          if ( analyzer instanceof IClonableDocumentAnalyzer ) {
            analyzer = ( (IClonableDocumentAnalyzer) analyzer ).cloneAnalyzer();
          } else {
            log.debug( Messages.getString( "WARNING.CannotCloneAnalyzer" ), analyzer );
          }
          Runnable analyzerRunner = getAnalyzerRunner( analyzer, document );

          Graph g = ( graph != null ) ? graph : new TinkerGraph();
          Future<Graph> transAnalysis =
            LineageGraphCompletionService.getInstance().submit( analyzerRunner, g );

          // Save this Future, the client will call it when the analysis is needed
          LineageGraphMap.getInstance().put( document.getContent(), transAnalysis );
        }
      }
    }
  }

  /**
   * Enhances an edge by adding a localized type
   *
   * @param edge The edge to enhance
   */
  public static void enhanceEdge( Edge edge ) {
    String type = edge.getLabel();
    //localize the node type
    String localizedType = Messages.getString( MESSAGE_PREFIX_LINKTYPE + type );
    if ( !localizedType.startsWith( MESSAGE_FAILED_PREFIX ) ) {
      edge.setProperty( DictionaryConst.PROPERTY_TYPE_LOCALIZED, localizedType );
    }
  }

  /**
   * Enhances a vertex by adding localized type and category, and a suggested color
   *
   * @param vertex The vertex to enhance
   */
  public static void enhanceVertex( Vertex vertex ) {
    String type = vertex.getProperty( DictionaryConst.PROPERTY_TYPE );
    //localize the node type
    String localizedType = Messages.getString( MESSAGE_PREFIX_NODETYPE + type );
    if ( !localizedType.startsWith( MESSAGE_FAILED_PREFIX ) ) {
      vertex.setProperty( DictionaryConst.PROPERTY_TYPE_LOCALIZED, localizedType );
    }
    // get the vertex category and set it
    String category = DictionaryHelper.getCategoryForType( type );
    vertex.setProperty( DictionaryConst.PROPERTY_CATEGORY, category );
    // get the vertex category color and set it
    String color = DictionaryHelper.getColorForCategory( category );
    vertex.setProperty( DictionaryConst.PROPERTY_COLOR, color );
    //localize the category
    String localizedCat = Messages.getString( MESSAGE_PREFIX_CATEGORY + category );
    if ( !localizedCat.startsWith( MESSAGE_FAILED_PREFIX ) ) {
      vertex.setProperty( DictionaryConst.PROPERTY_CATEGORY_LOCALIZED, localizedCat );
    }
  }

  public static Operations convertOperationsStringToMap( String operations ) {
    Operations resultOps = null;
    if ( !Const.isEmpty( operations ) ) {
      try {
        Map<String, List<IOperation>> rawOpsMap = new JSONDeserializer<Map<String, List<IOperation>>>().
          use( "values.values", Operation.class ).
          deserialize( operations );
        resultOps = new Operations();
        for ( String key : rawOpsMap.keySet() ) {
          resultOps.put( ChangeType.forValue( key ), rawOpsMap.get( key ) );
        }
      } catch ( Exception e ) {
        resultOps = null;
      }
      //return new JSONDeserializer<Operations>().use(null, Operations.class).deserialize( operations );
    }
    return resultOps;
  }

  public static Runnable getAnalyzerRunner( final IDocumentAnalyzer analyzer, final IDocument document ) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          MetaverseComponentDescriptor docDescriptor = new MetaverseComponentDescriptor(
            document.getName(),
            DictionaryConst.NODE_TYPE_TRANS,
            document.getNamespace() );
          if ( document.getContext() != null ) {
            docDescriptor.setContext( document.getContext() );
          }
          analyzer.analyze( docDescriptor, document );
        } catch ( MetaverseAnalyzerException mae ) {
          throw new RuntimeException( Messages.getString( "ERROR.AnalyzingDocument", document.getNamespaceId() ), mae );
        }
      }
    };
  }

  /**
   * This method is implemented for integration testing purposes. By default, there is no delay, but a delay can be
   * introduced to verify certain features are working as expected, such as that all parties interested in the graphml
   * output wait sufficiently long enough for the file to be finished writing before attempting to process it.
   */
  public static void delay() {
    final MetaverseConfig metaverseConfig = MetaverseConfig.getInstance();
    if ( metaverseConfig != null && metaverseConfig.getLineageDelay() > 0 ) {
      try {
        Thread.sleep( metaverseConfig.getLineageDelay() * 1000 );
      } catch ( final InterruptedException ie ) {
        // no-op
      }
    }
  }
}

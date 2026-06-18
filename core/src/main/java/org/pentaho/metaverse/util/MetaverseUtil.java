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


package org.pentaho.metaverse.util;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;


/**
 * A class containing utility methods for Metaverse / lineage
 */
public class MetaverseUtil {

  private static final Logger log = LoggerFactory.getLogger( MetaverseUtil.class );
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
      documentController = (IDocumentController) MetaverseBeanUtil.getInstance().get( IDocumentController.class );
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

          Graph g = ( graph != null ) ? graph : TinkerGraph.open();
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
    String type = edge.label();
    //localize the node type
    String localizedType = Messages.getString( MESSAGE_PREFIX_LINKTYPE + type );
    if ( !localizedType.startsWith( MESSAGE_FAILED_PREFIX ) ) {
      edge.property( DictionaryConst.PROPERTY_TYPE_LOCALIZED, localizedType );
    }
  }

  /**
   * Enhances a vertex by adding localized type and category, and a suggested color
   *
   * @param vertex The vertex to enhance
   */
  public static void enhanceVertex( Vertex vertex ) {
    String type = vertex.property( DictionaryConst.PROPERTY_TYPE ).isPresent()
      ? vertex.<String>value( DictionaryConst.PROPERTY_TYPE ) : null;
    //localize the node type
    String localizedType = Messages.getString( MESSAGE_PREFIX_NODETYPE + type );
    if ( !localizedType.startsWith( MESSAGE_FAILED_PREFIX ) ) {
      vertex.property( DictionaryConst.PROPERTY_TYPE_LOCALIZED, localizedType );
    }
    // get the vertex category and set it
    String category = DictionaryHelper.getCategoryForType( type );
    vertex.property( DictionaryConst.PROPERTY_CATEGORY, category );
    // get the vertex category color and set it
    String color = DictionaryHelper.getColorForCategory( category );
    vertex.property( DictionaryConst.PROPERTY_COLOR, color );
    //localize the category
    String localizedCat = Messages.getString( MESSAGE_PREFIX_CATEGORY + category );
    if ( !localizedCat.startsWith( MESSAGE_FAILED_PREFIX ) ) {
      vertex.property( DictionaryConst.PROPERTY_CATEGORY_LOCALIZED, localizedCat );
    }
  }

  public static Operations convertOperationsStringToMap( String operations ) {
    Operations resultOps = null;
    if ( operations != null && !operations.isEmpty() ) {
      try {
        JsonNode root = OBJECT_MAPPER.readTree( operations );
        resultOps = new Operations();

        if ( root != null && root.isObject() ) {
          Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
          while ( fields.hasNext() ) {
            Map.Entry<String, JsonNode> entry = fields.next();
            ChangeType changeType = ChangeType.forValue( entry.getKey() );
            if ( changeType == null || !entry.getValue().isArray() ) {
              continue;
            }

            processOperationNodeList( entry.getValue(), changeType, resultOps );
          }
        }
      } catch ( Exception e ) {
        resultOps = null;
      }
    }
    return resultOps;
  }

  private static void processOperationNodeList( Iterable<JsonNode> operationNodes, ChangeType changeType,
      Operations resultOps ) {
    List<IOperation> typedOperations = new ArrayList<>();
    for ( JsonNode operationNode : operationNodes ) {
      IOperation operation = parseOperationNode( operationNode, changeType );
      typedOperations.add( operation );
    }
    resultOps.put( changeType, typedOperations );
  }

  private static IOperation parseOperationNode( JsonNode operationNode, ChangeType changeType ) {
    String name = extractStringValue( operationNode, "name" );
    String description = extractStringValue( operationNode, "description" );
    String category = extractStringValue( operationNode, "category" );
    String typeValue = extractStringValue( operationNode, "type" );

    ChangeType operationType = changeType;  // default to the map key
    if ( typeValue != null && !typeValue.isEmpty() ) {
      operationType = parseChangeType( typeValue, changeType );
    }

    Operation operation = new Operation( name, description );
    operation.setCategory( category );
    operation.setType( operationType );
    return operation;
  }

  private static String extractStringValue( JsonNode node, String fieldName ) {
    JsonNode fieldNode = node.get( fieldName );
    return fieldNode == null || fieldNode.isNull() ? null : fieldNode.asText();
  }

  private static ChangeType parseChangeType( String typeValue, ChangeType defaultType ) {
    try {
      return ChangeType.valueOf( typeValue );
    } catch ( IllegalArgumentException ignored ) {
      return defaultType;
    }
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

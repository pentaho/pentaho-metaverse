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


package org.pentaho.metaverse.graph;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.commons.collections.IteratorUtils;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IGraphWriter;
import org.pentaho.metaverse.api.model.BaseMetaverseBuilder;
import org.pentaho.metaverse.impl.MetaverseConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A base implementation of the {@link IGraphWriter}.
 */
public abstract class BaseGraphWriter implements IGraphWriter {

  public static void adjustGraph( final Graph graph ) {
    mergeOutputsAndInputs( graph );

    if ( MetaverseConfig.deduplicateTransformationFields() ) {
      deduplicateTransFields( graph );
    }

    if ( MetaverseConfig.adjustExternalResourceFields() ) {
      // first add the missing links from each external resources to their fields
      addExternalResourceContainsFieldsLinks( graph );

      // if a single step reads more than one external resource, we will now likely have duplicate fields "contain"ed by
      // each resource, which need to be de-duplicated
      deduplicateExternalResourceFields( graph );
    }
  }

  @Override
  public final void outputGraph( Graph graph, OutputStream graphMLOutputStream ) throws IOException {

    adjustGraph( graph );
    outputGraphImpl( graph, graphMLOutputStream );
  }

  protected abstract void outputGraphImpl( final Graph graph, final OutputStream outputStream ) throws IOException;

  private static Set<Vertex> getVerticesByCategoryAndName( final Graph graph, final String category,
                                                           final String name ) {
    final Iterator<Vertex> allVertices = graph.vertices();

    final Set<Vertex> documentElementVertices = new HashSet();
    while ( allVertices.hasNext() ) {
      final Vertex vertex = allVertices.next();
      final String vCat = vertex.property( DictionaryConst.PROPERTY_CATEGORY ).isPresent()
        ? vertex.<String>value( DictionaryConst.PROPERTY_CATEGORY ) : null;
      final String vName = vertex.property( DictionaryConst.PROPERTY_NAME ).isPresent()
        ? vertex.<String>value( DictionaryConst.PROPERTY_NAME ) : null;
      if ( ( category == null || category.equals( vCat ) )
        && ( name == null || name.equals( vName ) ) ) {
        documentElementVertices.add( vertex );
      }
    }
    return documentElementVertices;
  }

  private static Set<Vertex> getVerticesByCategory( final Graph graph, final String category ) {
    return getVerticesByCategoryAndName( graph, category, null );
  }

  /**
   * Returns all vertices categorized as "documentelement", which corresponds to all Step and Job Entry vertices.
   *
   * @param graph the {@link Graph}
   * @return a {@link Set} of vertices corresponding to Steps and Job Enties.
   */
  private static Set<Vertex> getDocumentElementVertices( final Graph graph ) {
    return getVerticesByCategory( graph, DictionaryConst.CATEGORY_DOCUMENT_ELEMENT );
  }

  /**
   * In some cases, we are unable to make a connection between a step's output fields and the input fields that the step
   * hops to at analysis time. Inspect all input stream transformation fields of a step that do not have a corresponding
   * IN "outputs" link, and check whether there are any matching "output" fields in the steps that hop to the given
   * step. If so, merge the fields, OR if there are multiple fields that match, add appropriate missing links.
   */
  private static void mergeOutputsAndInputs( final Graph graph ) {
    // get all Step and Job Entry nodes
    final Iterator<Vertex> documentElementVertices = getDocumentElementVertices( graph ).iterator();

    while ( documentElementVertices.hasNext() ) {
      final Vertex documentElementVertex = documentElementVertices.next();

      final List<Vertex> inputFields = getLinkedVertices( documentElementVertex, Direction.IN,
        DictionaryConst.LINK_INPUTS, DictionaryConst.CATEGORY_FIELD, true, DictionaryConst.NODE_TYPE_TRANS_FIELD,
        true );
      final List<Vertex> orphanedInputFields = new ArrayList();
      for ( final Vertex inputField : inputFields ) {
        if ( !inputField.edges( Direction.IN, DictionaryConst.LINK_OUTPUTS ).hasNext() ) {
          orphanedInputFields.add( inputField );
        }
      }
      if ( orphanedInputFields.size() > 0 ) {
        final List<Vertex> inputSteps = getLinkedVertices( documentElementVertex, Direction.IN,
          DictionaryConst.LINK_HOPSTO, DictionaryConst.CATEGORY_DOCUMENT_ELEMENT, true,
          DictionaryConst.NODE_TYPE_TRANS_STEP, true );
        // traverse all input steps and get their output fields whose names match the orphaned field;
        // when the first match is found, merge the two fields, all subsequent matches need to have the "input" link
        // added from the output field to the orphaned field
        for ( final Vertex orphanedInputField : orphanedInputFields ) {
          final String orphanedInputFieldName = orphanedInputField.property( DictionaryConst.PROPERTY_NAME ).isPresent()
            ? orphanedInputField.<String>value( DictionaryConst.PROPERTY_NAME ) : null;
          int matchCount = 0;
          for ( final Vertex inputStep : inputSteps ) {
            final List<Vertex> inputStepOutputFields = getLinkedVertices( inputStep, Direction.OUT,
              DictionaryConst.LINK_OUTPUTS, DictionaryConst.CATEGORY_FIELD, true,
              DictionaryConst.NODE_TYPE_TRANS_FIELD, true );
            for ( final Vertex inputStepOutputField : inputStepOutputFields ) {
              final String inputStepOutputFieldName = inputStepOutputField.property( DictionaryConst.PROPERTY_NAME ).isPresent()
                ? inputStepOutputField.<String>value( DictionaryConst.PROPERTY_NAME ) : null;
              if ( inputStepOutputFieldName.equals( orphanedInputFieldName ) ) {
                // we have a match - if this is the first match, merge the fields
                if ( matchCount == 0 ) {
                  rewireEdges( graph, inputStepOutputField, orphanedInputField, Direction.IN );
                  rewireEdges( graph, inputStepOutputField, orphanedInputField, Direction.OUT );
                  // remove the orphaned input field, we no longer need it
                  orphanedInputField.remove();
                } else {
                  // otherwise add an input link from the inputStepOutputField to the parent step of the orphaned field
                  final String newLinkId = BaseMetaverseBuilder.getEdgeId( inputStepOutputField,
                    DictionaryConst.LINK_INPUTS, documentElementVertex );
                  if ( graph.edges( newLinkId ) == null || !graph.edges( newLinkId ).hasNext() ) {
                    inputStepOutputField.addEdge( DictionaryConst.LINK_INPUTS, documentElementVertex,
                      org.apache.tinkerpop.gremlin.structure.T.id, newLinkId )
                      .property( "text", DictionaryConst.LINK_INPUTS );
                  }
                }
                matchCount++;
              }
            }
          }
        }
      }
    }
  }

  private static void deduplicateTransFields( final Graph graph ) {
    // get all Step and Job Entry nodes
    final Iterator<Vertex> documentElementVertices = getDocumentElementVertices( graph ).iterator();

    while ( documentElementVertices.hasNext() ) {
      final Vertex documentElementVertex = documentElementVertices.next();
      // merge fields at the end of the "outputs" edges
      mergeFields( graph, documentElementVertex, Direction.OUT, DictionaryConst.LINK_OUTPUTS, true );
    }
  }

  private static void mergeFields( final Graph graph, final Vertex documentElementVertex, final Direction direction,
                                   final String linkLabel, boolean isTransformationField ) {
    // get all edges corresponding to the requested direction and with the requested label
    final List<Edge> links = IteratorUtils.toList(
      documentElementVertex.edges( direction, linkLabel ) );
    // traverse the links and see if there are any that point to fields with the same names, if so, they need to be
    // merged
    final Map<String, Set<Vertex>> fieldMap = new HashMap();
    for ( final Edge link : links ) {
      // get the vertex at the "other" end of this link ("this" end being the vertex itself)
      final Vertex vertex = direction == Direction.IN ? link.outVertex() : link.inVertex();
      final String category = vertex.property( DictionaryConst.PROPERTY_CATEGORY ).isPresent()
        ? vertex.<String>value( DictionaryConst.PROPERTY_CATEGORY ) : null;
      final String type = vertex.property( DictionaryConst.PROPERTY_TYPE ).isPresent()
        ? vertex.<String>value( DictionaryConst.PROPERTY_TYPE ) : null;
      // verify that the vertex is a field of the desired type
      if ( DictionaryConst.CATEGORY_FIELD.equals( category )
        && isTransformationField == DictionaryConst.NODE_TYPE_TRANS_FIELD.equals( type ) ) {
        final String fieldName = vertex.property( DictionaryConst.PROPERTY_NAME ).isPresent()
          ? vertex.<String>value( DictionaryConst.PROPERTY_NAME ) : null;

        Set<Vertex> fieldsWithSameName = fieldMap.get( fieldName );
        if ( fieldsWithSameName == null ) {
          fieldsWithSameName = new HashSet();
          fieldMap.put( fieldName, fieldsWithSameName );
        }
        fieldsWithSameName.add( vertex );
      }
    }

    // traverse the map pf fields - for any field name, if more than one has been found, merge them into one
    final List<Map.Entry<String, Set<Vertex>>> fields = IteratorUtils.toList( fieldMap.entrySet().iterator() );
    for ( final Map.Entry<String, Set<Vertex>> fieldEntry : fields ) {
      final List<Vertex> fieldVertices = new ArrayList( fieldEntry.getValue() );
      if ( fieldVertices.size() > 1 ) {
        // get the first vertex - we will keep this one and re-point links connected to all the rest back to this
        // original one, and then remove the remaining ones
        final Vertex fieldVertexToKeep = fieldVertices.get( 0 );
        for ( int i = 1; i < fieldVertices.size(); i++ ) {
          final Vertex fieldVertexToMerge = fieldVertices.get( i );
          rewireEdges( graph, fieldVertexToKeep, fieldVertexToMerge, Direction.IN );
          rewireEdges( graph, fieldVertexToKeep, fieldVertexToMerge, Direction.OUT );
          // we can now safely remove 'fieldVertexToMerge'
          fieldVertexToMerge.remove();
        }
      }
    }
  }

  private static void rewireEdges( final Graph graph, final Vertex vertexToKeep, final Vertex vertexToMerge,
                                   final Direction direction ) {
    final Iterator<Edge> links = vertexToMerge.edges( direction );
    while ( links.hasNext() ) {
      // remove this edge and recreate one that points to 'fieldVertexToKeep' instead of 'fieldVertexToMerge',
      // where it pointed originally
      final Edge originalLink = links.next();
      final String newLinkId = direction == Direction.OUT
        ? BaseMetaverseBuilder.getEdgeId(
        vertexToKeep, originalLink.label(), originalLink.inVertex() )
        : BaseMetaverseBuilder.getEdgeId(
        originalLink.outVertex(), originalLink.label(), vertexToKeep );
      Iterator<Edge> existingEdge = graph.edges( newLinkId );
      if ( !existingEdge.hasNext() ) {
        if ( direction == Direction.OUT ) {
          vertexToKeep.addEdge( originalLink.label(), originalLink.inVertex(),
            org.apache.tinkerpop.gremlin.structure.T.id, newLinkId )
            .property( "text", originalLink.label() );
        } else {
          originalLink.outVertex().addEdge( originalLink.label(), vertexToKeep,
            org.apache.tinkerpop.gremlin.structure.T.id, newLinkId )
            .property( "text", originalLink.label() );
        }
      }
      // remove the original link
      originalLink.remove();
    }
  }

  private static Set<Vertex> getCollectionVertices( final Graph graph ) {
    return getVerticesByCategory( graph, DictionaryConst.CATEGORY_FIELD_COLLECTION );
  }

  private static Set<Vertex> getSQLVertices( final Graph graph ) {
    return getVerticesByCategoryAndName( graph, DictionaryConst.CATEGORY_OTHER, DictionaryConst.NODE_NAME_SQL );
  }

  private static Set<Vertex> getExternalResourceVertices( final Graph graph ) {
    final Set<Vertex> externalResourceVertexSet = getCollectionVertices( graph );
    externalResourceVertexSet.addAll( getSQLVertices( graph ) );
    return externalResourceVertexSet;
  }

  private static List<Vertex> getLinkedVertices( final Vertex originVertex, final Direction edgeDirection,
                                                 final String edgeLabel,
                                                 final String linkedVertexCategory, final boolean equalToCategory,
                                                 final String linkedVertexType, final boolean equalToType ) {

    final List<Vertex> linkedVertices = new ArrayList();
    final Iterator<Edge> links = originVertex.edges( edgeDirection, edgeLabel );
    while ( links.hasNext() ) {
      final Edge link = links.next();
      // get the vertex at the opposite end of the edge
      final Vertex vertex = edgeDirection == Direction.IN ? link.outVertex() : link.inVertex();
      final String category = vertex.property( DictionaryConst.PROPERTY_CATEGORY ).isPresent()
        ? vertex.<String>value( DictionaryConst.PROPERTY_CATEGORY ) : null;
      final String type = vertex.property( DictionaryConst.PROPERTY_TYPE ).isPresent()
        ? vertex.<String>value( DictionaryConst.PROPERTY_TYPE ) : null;
      if ( ( linkedVertexCategory == null
        || ( category != null && equalToCategory == category.equals( linkedVertexCategory ) ) )
        && ( linkedVertexType == null
        || ( type != null && equalToType == type.equals( linkedVertexType ) ) ) ) {
        linkedVertices.add( vertex );
      }
    }
    return linkedVertices;
  }

  private static List<Vertex> getStepInputExternalResourceFields( final Vertex stepVertex ) {
    return getLinkedVertices( stepVertex, Direction.IN,
      DictionaryConst.LINK_INPUTS, DictionaryConst.CATEGORY_FIELD, true,
      DictionaryConst.NODE_TYPE_TRANS_FIELD, false );
  }

  private static List<Vertex> getStepOutputExternalResourceFields( final Vertex stepVertex ) {
    return getLinkedVertices( stepVertex, Direction.OUT,
      DictionaryConst.LINK_OUTPUTS, DictionaryConst.CATEGORY_FIELD, true,
      DictionaryConst.NODE_TYPE_TRANS_FIELD, false );
  }

  private static List<Vertex> getStepsReadingExternalResource( final Vertex extenralResourceVertex ) {
    return getLinkedVertices( extenralResourceVertex, Direction.OUT,
      DictionaryConst.LINK_READBY, DictionaryConst.CATEGORY_DOCUMENT_ELEMENT, true, null, false );
  }

  private static List<Vertex> getStepsWritingToExternalResource( final Vertex extenralResourceVertex ) {
    return getLinkedVertices( extenralResourceVertex, Direction.IN,
      DictionaryConst.LINK_WRITESTO, DictionaryConst.CATEGORY_DOCUMENT_ELEMENT, true, null, false );
  }

  // add "contains" edges only to fields and columns which input into the step
  private static void addExternalResourceContainsFieldsLinks( final Graph graph ) {
    // get all external resources (files, SQL queries, database tables etc...)
    final Iterator<Vertex> externalResourceVertices = getExternalResourceVertices( graph ).iterator();

    while ( externalResourceVertices.hasNext() ) {
      final Vertex externalResourceVertex = externalResourceVertices.next();
      // for each external resource vertex, get all steps that read it
      List<Vertex> stepVertices = getStepsReadingExternalResource( externalResourceVertex );
      for ( final Vertex stepVertex : stepVertices ) {

        // for each step, get all non-transformation fields linked through the IN "inputs" edges
        final List<Vertex> fieldVertices = getStepInputExternalResourceFields( stepVertex );
        addContainsLinks( graph, externalResourceVertex, fieldVertices );
      }
      stepVertices = getStepsWritingToExternalResource( externalResourceVertex );
      for ( final Vertex stepVertex : stepVertices ) {
        // for each step, get all non-transformation fields linked through the OUT "outputs" edges
        final List<Vertex> fieldVertices = getStepOutputExternalResourceFields( stepVertex );
        addContainsLinks( graph, externalResourceVertex, fieldVertices );
      }
    }
  }

  private static void addContainsLinks( final Graph graph, final Vertex externalResourceVertex,
                                        final List<Vertex> fieldVertices ) {
    for ( final Vertex fieldVertex : fieldVertices ) {
      final String newLinkId = BaseMetaverseBuilder.getEdgeId(
        externalResourceVertex, DictionaryConst.LINK_CONTAINS, fieldVertex );
      Iterator<Edge> existing = graph.edges( newLinkId );
      if ( !existing.hasNext() ) {
        // add a "contains" link from the external resource to the field, if one doesn't already exist
        externalResourceVertex.addEdge( DictionaryConst.LINK_CONTAINS, fieldVertex,
          org.apache.tinkerpop.gremlin.structure.T.id, newLinkId )
          .property( "text", DictionaryConst.LINK_CONTAINS );
      }
    }
  }

  private static void deduplicateExternalResourceFields( final Graph graph ) {
    // get all external resources (files, SQL queries, database tables etc...)
    final Iterator<Vertex> externalResourceVertices = getExternalResourceVertices( graph ).iterator();

    // traverse the links and see if there are any that point to fields with the same names, if so, they need to be
    // merged
    while ( externalResourceVertices.hasNext() ) {
      final Vertex vertex = externalResourceVertices.next();
      // merge non-transformation fields at the end of the "outputs" edges
      mergeFields( graph, vertex, Direction.OUT, DictionaryConst.LINK_CONTAINS, false );
    }
  }
}

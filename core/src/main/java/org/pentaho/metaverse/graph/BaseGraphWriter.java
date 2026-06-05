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

import com.tinkerpop.blueprints.Graph;
import org.apache.commons.collections.IteratorUtils;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IGraphWriter;
import org.pentaho.metaverse.api.model.BaseMetaverseBuilder;
import org.pentaho.metaverse.graph.adapter.BlueprintsAdapters;
import org.pentaho.metaverse.graph.adapter.DirectionAdapter;
import org.pentaho.metaverse.graph.adapter.EdgeAdapter;
import org.pentaho.metaverse.graph.adapter.GraphAdapter;
import org.pentaho.metaverse.graph.adapter.VertexAdapter;
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
    adjustGraph( BlueprintsAdapters.wrap( graph ) );
  }

  private static void adjustGraph( final GraphAdapter graph ) {
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

  private static Set<VertexAdapter> getVerticesByCategoryAndName( final GraphAdapter graph, final String category,
                                                                  final String name ) {
    final Iterator<VertexAdapter> allVertices = graph.getVertices().iterator();

    final Set<VertexAdapter> documentElementVertices = new HashSet<VertexAdapter>();
    while ( allVertices.hasNext() ) {
      final VertexAdapter vertex = allVertices.next();
      if ( ( category == null || category.equals( vertex.getProperty( DictionaryConst.PROPERTY_CATEGORY ) ) )
        && ( name == null || name.equals( vertex.getProperty( DictionaryConst.PROPERTY_NAME ) ) ) ) {
        documentElementVertices.add( vertex );
      }
    }
    return documentElementVertices;
  }

  private static Set<VertexAdapter> getVerticesByCategory( final GraphAdapter graph, final String category ) {
    return getVerticesByCategoryAndName( graph, category, null );
  }

  /**
   * Returns all vertices categorized as "documentelement", which corresponds to all Step and Job Entry vertices.
   *
   * @param graph the {@link Graph}
   * @return a {@link Set} of vertices corresponding to Steps and Job Enties.
   */
  private static Set<VertexAdapter> getDocumentElementVertices( final GraphAdapter graph ) {
    return getVerticesByCategory( graph, DictionaryConst.CATEGORY_DOCUMENT_ELEMENT );
  }

  /**
   * In some cases, we are unable to make a connection between a step's output fields and the input fields that the step
   * hops to at analysis time. Inspect all input stream transformation fields of a step that do not have a corresponding
   * IN "outputs" link, and check whether there are any matching "output" fields in the steps that hop to the given
   * step. If so, merge the fields, OR if there are multiple fields that match, add appropriate missing links.
   */
  private static void mergeOutputsAndInputs( final GraphAdapter graph ) {
    // get all Step and Job Entry nodes
    final Iterator<VertexAdapter> documentElementVertices = getDocumentElementVertices( graph ).iterator();

    while ( documentElementVertices.hasNext() ) {
      final VertexAdapter documentElementVertex = documentElementVertices.next();

      final List<VertexAdapter> inputFields = getLinkedVertices( documentElementVertex, DirectionAdapter.IN,
        DictionaryConst.LINK_INPUTS, DictionaryConst.CATEGORY_FIELD, true, DictionaryConst.NODE_TYPE_TRANS_FIELD,
        true );
      final List<VertexAdapter> orphanedInputFields = new ArrayList<VertexAdapter>();
      for ( final VertexAdapter inputField : inputFields ) {
        if ( !inputField.getEdges( DirectionAdapter.IN, DictionaryConst.LINK_OUTPUTS ).iterator().hasNext() ) {
          orphanedInputFields.add( inputField );
        }
      }
      if ( orphanedInputFields.size() > 0 ) {
        final List<VertexAdapter> inputSteps = getLinkedVertices( documentElementVertex, DirectionAdapter.IN,
          DictionaryConst.LINK_HOPSTO, DictionaryConst.CATEGORY_DOCUMENT_ELEMENT, true,
          DictionaryConst.NODE_TYPE_TRANS_STEP, true );
        // traverse all input steps and get their output fields whose names match the orphaned field;
        // when the first match is found, merge the two fields, all subsequent matches need to have the "input" link
        // added from the output field to the orphaned field
        for ( final VertexAdapter orphanedInputField : orphanedInputFields ) {
          final String orphanedInputFieldName = orphanedInputField.getProperty( DictionaryConst.PROPERTY_NAME );
          int matchCount = 0;
          for ( final VertexAdapter inputStep : inputSteps ) {
            final List<VertexAdapter> inputStepOutputFields = getLinkedVertices( inputStep, DirectionAdapter.OUT,
              DictionaryConst.LINK_OUTPUTS, DictionaryConst.CATEGORY_FIELD, true,
              DictionaryConst.NODE_TYPE_TRANS_FIELD, true );
            for ( final VertexAdapter inputStepOutputField : inputStepOutputFields ) {
              final String inputStepOutputFieldName = inputStepOutputField.getProperty( DictionaryConst
                .PROPERTY_NAME );
              if ( inputStepOutputFieldName.equals( orphanedInputFieldName ) ) {
                // we have a match - if this is the first match, merge the fields
                if ( matchCount == 0 ) {
                  rewireEdges( graph, inputStepOutputField, orphanedInputField, DirectionAdapter.IN );
                  rewireEdges( graph, inputStepOutputField, orphanedInputField, DirectionAdapter.OUT );
                  // remove the orphaned input field, we no longer need it
                  orphanedInputField.remove();
                } else {
                  // otherwise add an input link from the inputStepOutputField to the parent step of the orphaned field
                  final String newLinkId = getEdgeId( inputStepOutputField, DictionaryConst.LINK_INPUTS,
                    documentElementVertex );
                  graph.addEdge( newLinkId, inputStepOutputField, documentElementVertex, DictionaryConst.LINK_INPUTS )
                    .setProperty( "text", DictionaryConst.LINK_INPUTS );
                }
                matchCount++;
              }
            }
          }
        }
      }
    }
  }

  private static void deduplicateTransFields( final GraphAdapter graph ) {
    // get all Step and Job Entry nodes
    final Iterator<VertexAdapter> documentElementVertices = getDocumentElementVertices( graph ).iterator();

    while ( documentElementVertices.hasNext() ) {
      final VertexAdapter documentElementVertex = documentElementVertices.next();
      // merge fields at the end of the "outputs" edges
      mergeFields( graph, documentElementVertex, DirectionAdapter.OUT, DictionaryConst.LINK_OUTPUTS, true );
    }
  }

  private static void mergeFields( final GraphAdapter graph, final VertexAdapter documentElementVertex,
                                   final DirectionAdapter direction,
                                   final String linkLabel, boolean isTransformationField ) {
    // get all edges corresponding to the requested direction and with the requested label
    final List<EdgeAdapter> links = IteratorUtils.toList(
      documentElementVertex.getEdges( direction, linkLabel ).iterator() );
    // traverse the links and see if there are any that point to fields with the same names, if so, they need to be
    // merged
    final Map<String, Set<VertexAdapter>> fieldMap = new HashMap<String, Set<VertexAdapter>>();
    for ( final EdgeAdapter link : links ) {
      // get the vertex at the "other" end of this linnk ("this" end being the vertex itself)
      final VertexAdapter vertex = link.getVertex( direction.getOpposite() );
      final String category = vertex.getProperty( DictionaryConst.PROPERTY_CATEGORY );
      final String type = vertex.getProperty( DictionaryConst.PROPERTY_TYPE );
      // verify that the vertex is a field of the desired type
      if ( DictionaryConst.CATEGORY_FIELD.equals( category )
        && isTransformationField == DictionaryConst.NODE_TYPE_TRANS_FIELD.equals( type ) ) {
        final String fieldName = vertex.getProperty( DictionaryConst.PROPERTY_NAME );

        Set<VertexAdapter> fieldsWithSameName = fieldMap.get( fieldName );
        if ( fieldsWithSameName == null ) {
          fieldsWithSameName = new HashSet<VertexAdapter>();
          fieldMap.put( fieldName, fieldsWithSameName );
        }
        fieldsWithSameName.add( vertex );
      }
    }

    // traverse the map pf fields - for any field name, if more than one has been found, merge them into one
    final List<Map.Entry<String, Set<VertexAdapter>>> fields = IteratorUtils.toList( fieldMap.entrySet().iterator() );
    for ( final Map.Entry<String, Set<VertexAdapter>> fieldEntry : fields ) {
      final List<VertexAdapter> fieldVertices = new ArrayList<VertexAdapter>( fieldEntry.getValue() );
      if ( fieldVertices.size() > 1 ) {
        // get the first vertex - we will keep this one and re-point links connected to all the rest back to this
        // original one, and then remove the remaining ones
        final VertexAdapter fieldVertexToKeep = fieldVertices.get( 0 );
        for ( int i = 1; i < fieldVertices.size(); i++ ) {
          final VertexAdapter fieldVertexToMerge = fieldVertices.get( i );
          rewireEdges( graph, fieldVertexToKeep, fieldVertexToMerge, DirectionAdapter.IN );
          rewireEdges( graph, fieldVertexToKeep, fieldVertexToMerge, DirectionAdapter.OUT );
          // we can now safely remove 'fieldVertexToMerge'
          fieldVertexToMerge.remove();
        }
      }
    }
  }

  private static void rewireEdges( final GraphAdapter graph, final VertexAdapter vertexToKeep,
                                   final VertexAdapter vertexToMerge, final DirectionAdapter direction ) {
    final Iterator<EdgeAdapter> links = vertexToMerge.getEdges( direction ).iterator();
    while ( links.hasNext() ) {
      // remove this edge and recreate one that points to 'fieldVertexToKeep' instead of 'fieldVertexToMerge',
      // where it pointed originally
      final EdgeAdapter originalLink = links.next();
      final String newLinkId = direction == DirectionAdapter.OUT
        ? getEdgeId( vertexToKeep, originalLink.getLabel(), originalLink.getVertex( DirectionAdapter.IN ) )
        : getEdgeId( originalLink.getVertex( DirectionAdapter.OUT ), originalLink.getLabel(), vertexToKeep );
      if ( graph.getEdge( newLinkId ) == null ) {
        if ( direction == DirectionAdapter.OUT ) {
          graph.addEdge( newLinkId, vertexToKeep, originalLink.getVertex( DirectionAdapter.IN ),
            originalLink.getLabel() )
            .setProperty( "text", originalLink.getLabel() );
        } else {
          graph.addEdge( newLinkId, originalLink.getVertex( DirectionAdapter.OUT ), vertexToKeep,
            originalLink.getLabel() ).setProperty( "text", originalLink.getLabel() );
        }
      }
      // remove the original link
      originalLink.remove();
    }
  }

  private static Set<VertexAdapter> getCollectionVertices( final GraphAdapter graph ) {
    return getVerticesByCategory( graph, DictionaryConst.CATEGORY_FIELD_COLLECTION );
  }

  private static Set<VertexAdapter> getSQLVertices( final GraphAdapter graph ) {
    return getVerticesByCategoryAndName( graph, DictionaryConst.CATEGORY_OTHER, DictionaryConst.NODE_NAME_SQL );
  }

  private static Set<VertexAdapter> getExternalResourceVertices( final GraphAdapter graph ) {
    final Set<VertexAdapter> externalResourceVertexSet = getCollectionVertices( graph );
    externalResourceVertexSet.addAll( getSQLVertices( graph ) );
    return externalResourceVertexSet;
  }

  private static List<VertexAdapter> getLinkedVertices( final VertexAdapter originVertex,
                                                        final DirectionAdapter edgeDirection, final String edgeLabel,
                                                        final String linkedVertexCategory,
                                                        final boolean equalToCategory, final String linkedVertexType,
                                                        final boolean equalToType ) {

    final List<VertexAdapter> linkedVertices = new ArrayList<VertexAdapter>();
    final Iterator<EdgeAdapter> links = originVertex.getEdges( edgeDirection, edgeLabel ).iterator();
    while ( links.hasNext() ) {
      final EdgeAdapter link = links.next();
      // get the vertex at the opposite end of the edge
      final VertexAdapter vertex = link.getVertex( edgeDirection.getOpposite() );
      final String category = vertex.getProperty( DictionaryConst.PROPERTY_CATEGORY );
      final String type = vertex.getProperty( DictionaryConst.PROPERTY_TYPE );
      if ( ( linkedVertexCategory == null
        || ( category != null && equalToCategory == category.equals( linkedVertexCategory ) ) )
        && ( linkedVertexType == null
        || ( type != null && equalToType == type.equals( linkedVertexType ) ) ) ) {
        linkedVertices.add( vertex );
      }
    }
    return linkedVertices;
  }

  private static List<VertexAdapter> getStepInputExternalResourceFields( final VertexAdapter stepVertex ) {
    return getLinkedVertices( stepVertex, DirectionAdapter.IN,
      DictionaryConst.LINK_INPUTS, DictionaryConst.CATEGORY_FIELD, true,
      DictionaryConst.NODE_TYPE_TRANS_FIELD, false );
  }

  private static List<VertexAdapter> getStepOutputExternalResourceFields( final VertexAdapter stepVertex ) {
    return getLinkedVertices( stepVertex, DirectionAdapter.OUT,
      DictionaryConst.LINK_OUTPUTS, DictionaryConst.CATEGORY_FIELD, true,
      DictionaryConst.NODE_TYPE_TRANS_FIELD, false );
  }

  private static List<VertexAdapter> getStepsReadingExternalResource( final VertexAdapter extenralResourceVertex ) {
    return getLinkedVertices( extenralResourceVertex, DirectionAdapter.OUT,
      DictionaryConst.LINK_READBY, DictionaryConst.CATEGORY_DOCUMENT_ELEMENT, true, null, false );
  }

  private static List<VertexAdapter> getStepsWritingToExternalResource( final VertexAdapter extenralResourceVertex ) {
    return getLinkedVertices( extenralResourceVertex, DirectionAdapter.IN,
      DictionaryConst.LINK_WRITESTO, DictionaryConst.CATEGORY_DOCUMENT_ELEMENT, true, null, false );
  }

  // add "contains" edges only to fields and columns which input into the step
  private static void addExternalResourceContainsFieldsLinks( final GraphAdapter graph ) {
    // get all external resources (files, SQL queries, database tables etc...)
    final Iterator<VertexAdapter> externalResourceVertices = getExternalResourceVertices( graph ).iterator();

    while ( externalResourceVertices.hasNext() ) {
      final VertexAdapter externalResourceVertex = externalResourceVertices.next();
      // for each external resource vertex, get all steps that read it
      List<VertexAdapter> stepVertices = getStepsReadingExternalResource( externalResourceVertex );
      for ( final VertexAdapter stepVertex : stepVertices ) {

        // for each step, get all non-transformation fields linked through the IN "inputs" edges
        final List<VertexAdapter> fieldVertices = getStepInputExternalResourceFields( stepVertex );
        addContainsLinks( graph, externalResourceVertex, fieldVertices );
      }
      stepVertices = getStepsWritingToExternalResource( externalResourceVertex );
      for ( final VertexAdapter stepVertex : stepVertices ) {
        // for each step, get all non-transformation fields linked through the OUT "outputs" edges
        final List<VertexAdapter> fieldVertices = getStepOutputExternalResourceFields( stepVertex );
        addContainsLinks( graph, externalResourceVertex, fieldVertices );
      }
    }
  }

  private static void addContainsLinks( final GraphAdapter graph, final VertexAdapter externalResourceVertex,
                                        final List<VertexAdapter> fieldVertices ) {
    for ( final VertexAdapter fieldVertex : fieldVertices ) {
      final String newLinkId = getEdgeId( externalResourceVertex, DictionaryConst.LINK_CONTAINS, fieldVertex );
      if ( graph.getEdge( newLinkId ) == null ) {
        // add a "contains" link from the external resource to the field, if one doesn't already exist
        graph.addEdge( newLinkId, externalResourceVertex, fieldVertex, DictionaryConst.LINK_CONTAINS )
          .setProperty( "text", DictionaryConst.LINK_CONTAINS );
      }
    }
  }

  private static void deduplicateExternalResourceFields( final GraphAdapter graph ) {
    // get all external resources (files, SQL queries, database tables etc...)
    final Iterator<VertexAdapter> externalResourceVertices = getExternalResourceVertices( graph ).iterator();

    // traverse the links and see if there are any that point to fields with the same names, if so, they need to be
    // merged
    while ( externalResourceVertices.hasNext() ) {
      final VertexAdapter vertex = externalResourceVertices.next();
      // merge non-transformation fields at the end of the "outputs" edges
      mergeFields( graph, vertex, DirectionAdapter.OUT, DictionaryConst.LINK_CONTAINS, false );
    }
  }

  private static String getEdgeId( VertexAdapter fromVertex, String label, VertexAdapter toVertex ) {
    return BaseMetaverseBuilder.getEdgeId(
      BlueprintsAdapters.unwrap( fromVertex ), label, BlueprintsAdapters.unwrap( toVertex ) );
  }
}

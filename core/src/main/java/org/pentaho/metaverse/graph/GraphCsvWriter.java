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

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.pentaho.dictionary.DictionaryConst;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * The GraphMLWriter class contains methods for writing a metaverse graph model in GraphML format
 * 
 */
public class GraphCsvWriter extends BaseGraphWriter {

  private static final String CSV_ENCLOSURE = "\"";

  @Override
  public void outputGraphImpl( Graph graph, OutputStream out ) throws IOException {

    Iterator<Edge> it = graph.edges();
    writeCSVField( "SourceId", out, true, false );
    writeCSVField( "SourceVirtual", out, false, false );
    writeCSVField( "SourceFileType", out, false, false );
    writeCSVField( "SourceName", out, false, false );
    writeCSVField( "SourceAuthor", out, false, false );
    writeCSVField( "SourceModified", out, false, false );
    writeCSVField( "LinkType", out, false, false );
    writeCSVField( "DestinationId", out, false, false );
    writeCSVField( "DestinationVirtual", out, false, false );
    writeCSVField( "DestinationFileType", out, false, false );
    writeCSVField( "DestinationName", out, false, false );
    writeCSVField( "DestinationAuthor", out, false, false );
    writeCSVField( "DestinationModified", out, false, true );
    while ( it.hasNext() ) {
      Edge edge = it.next();
      Vertex fromV = edge.outVertex();
      Vertex toV = edge.inVertex();
      writeCSVField( fromV.id(), out, true, false );
      writeCSVField( fromV.property( DictionaryConst.NODE_VIRTUAL ).isPresent() ? fromV.value( DictionaryConst.NODE_VIRTUAL ) : null, out, false, false );
      writeCSVField( fromV.property( DictionaryConst.PROPERTY_TYPE ).isPresent() ? fromV.value( DictionaryConst.PROPERTY_TYPE ) : null, out, false, false );
      writeCSVField( fromV.property( DictionaryConst.PROPERTY_NAME ).isPresent() ? fromV.value( DictionaryConst.PROPERTY_NAME ) : null, out, false, false );
      writeCSVField( fromV.property( DictionaryConst.PROPERTY_AUTHOR ).isPresent() ? fromV.value( DictionaryConst.PROPERTY_AUTHOR ) : null, out, false, false );
      writeCSVField( fromV.property( DictionaryConst.PROPERTY_LAST_MODIFIED ).isPresent() ? fromV.value( DictionaryConst.PROPERTY_LAST_MODIFIED ) : null, out, false, false );
      writeCSVField( edge.label(), out, false, false );
      writeCSVField( toV.id(), out, false, false );
      writeCSVField( toV.property( DictionaryConst.NODE_VIRTUAL ).isPresent() ? toV.value( DictionaryConst.NODE_VIRTUAL ) : null, out, false, false );
      writeCSVField( toV.property( DictionaryConst.PROPERTY_TYPE ).isPresent() ? toV.value( DictionaryConst.PROPERTY_TYPE ) : null, out, false, false );
      writeCSVField( toV.property( DictionaryConst.PROPERTY_NAME ).isPresent() ? toV.value( DictionaryConst.PROPERTY_NAME ) : null, out, false, false );
      writeCSVField( toV.property( DictionaryConst.PROPERTY_AUTHOR ).isPresent() ? toV.value( DictionaryConst.PROPERTY_AUTHOR ) : null, out, false, false );
      writeCSVField( toV.property( DictionaryConst.PROPERTY_LAST_MODIFIED ).isPresent() ? toV.value( DictionaryConst.PROPERTY_LAST_MODIFIED ) : null, out, false, true );
    }

  }

  /**
   * Writes out a line of CSV
   * @param obj The object (String, Date etc) to write out
   * @param out The output stream to write to
   * @param isFirst Is this the first field on the line
   * @param isLast Is this the last field on the line
   * @throws IOException If the output stream cannot be written to
   */
  protected void writeCSVField( Object obj, OutputStream out, boolean isFirst, boolean isLast ) throws IOException {
    if ( !isFirst ) {
      out.write( ",".getBytes() );
    }
    if ( obj != null ) {
      if ( obj instanceof String ) {
        out.write( CSV_ENCLOSURE.getBytes() );
        out.write( obj.toString().replace( CSV_ENCLOSURE, "\\\"" ).getBytes() );
        out.write( CSV_ENCLOSURE.getBytes() );
      } else {
        out.write( obj.toString().getBytes() );
      }
    }
    if ( isLast ) {
      out.write( "\n".getBytes() );
    }
  }

}

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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
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

    Iterable<Edge> iterable = graph.getEdges();
    Iterator<Edge> it = iterable.iterator();
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
      Vertex fromV = edge.getVertex( Direction.OUT );
      Vertex toV = edge.getVertex( Direction.IN );
      writeCSVField( fromV.getId(), out, true, false );
      writeCSVField( fromV.getProperty( DictionaryConst.NODE_VIRTUAL ), out, false, false );
      writeCSVField( fromV.getProperty( DictionaryConst.PROPERTY_TYPE ), out, false, false );
      writeCSVField( fromV.getProperty( DictionaryConst.PROPERTY_NAME ), out, false, false );
      writeCSVField( fromV.getProperty( DictionaryConst.PROPERTY_AUTHOR ), out, false, false );
      writeCSVField( fromV.getProperty( DictionaryConst.PROPERTY_LAST_MODIFIED ), out, false, false );
      writeCSVField( edge.getLabel(), out, false, false );
      writeCSVField( toV.getId(), out, false, false );
      writeCSVField( toV.getProperty( DictionaryConst.NODE_VIRTUAL ), out, false, false );
      writeCSVField( toV.getProperty( DictionaryConst.PROPERTY_TYPE ), out, false, false );
      writeCSVField( toV.getProperty( DictionaryConst.PROPERTY_NAME ), out, false, false );
      writeCSVField( toV.getProperty( DictionaryConst.PROPERTY_AUTHOR ), out, false, false );
      writeCSVField( toV.getProperty( DictionaryConst.PROPERTY_LAST_MODIFIED ), out, false, true );
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

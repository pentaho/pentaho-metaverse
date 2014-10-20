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

package com.pentaho.metaverse.graph;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IGraphWriter;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * The GraphMLWriter class contains methods for writing a metaverse graph model in GraphML format
 * 
 */
public class GraphCsvWriter implements IGraphWriter {

  private static final String CSV_ENCLOSURE = "\"";

  @Override
  public void outputGraph( Graph graph, OutputStream out ) throws IOException {

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

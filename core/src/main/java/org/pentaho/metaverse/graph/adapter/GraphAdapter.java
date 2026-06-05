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


package org.pentaho.metaverse.graph.adapter;

public interface GraphAdapter {
  VertexAdapter getVertex( Object id );

  VertexAdapter addVertex( Object id );

  EdgeAdapter getEdge( Object id );

  EdgeAdapter addEdge( Object id, VertexAdapter outVertex, VertexAdapter inVertex, String label );

  Iterable<VertexAdapter> getVertices();
}

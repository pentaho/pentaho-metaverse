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

import java.util.Set;

public interface VertexAdapter {
  Object getId();

  <T> T getProperty( String key );

  void setProperty( String key, Object value );

  Set<String> getPropertyKeys();

  Iterable<EdgeAdapter> getEdges( DirectionAdapter direction, String... labels );

  void remove();
}

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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import java.util.ArrayList;
import java.util.List;

public final class BlueprintsAdapters {

  private BlueprintsAdapters() {
    throw new UnsupportedOperationException();
  }

  public static GraphAdapter wrap( Graph graph ) {
    return new BlueprintsGraphAdapter( graph );
  }

  public static VertexAdapter wrap( Vertex vertex ) {
    return new BlueprintsVertexAdapter( vertex );
  }

  public static EdgeAdapter wrap( Edge edge ) {
    return new BlueprintsEdgeAdapter( edge );
  }

  public static Vertex unwrap( VertexAdapter vertexAdapter ) {
    if ( vertexAdapter instanceof BlueprintsVertexAdapter ) {
      return ( (BlueprintsVertexAdapter) vertexAdapter ).getDelegate();
    }
    throw new IllegalArgumentException( "Unsupported VertexAdapter implementation: " + vertexAdapter.getClass() );
  }

  public static Edge unwrap( EdgeAdapter edgeAdapter ) {
    if ( edgeAdapter instanceof BlueprintsEdgeAdapter ) {
      return ( (BlueprintsEdgeAdapter) edgeAdapter ).getDelegate();
    }
    throw new IllegalArgumentException( "Unsupported EdgeAdapter implementation: " + edgeAdapter.getClass() );
  }

  private static Direction unwrap( DirectionAdapter direction ) {
    return direction == DirectionAdapter.IN ? Direction.IN : Direction.OUT;
  }

  private static class BlueprintsGraphAdapter implements GraphAdapter {
    private final Graph delegate;

    private BlueprintsGraphAdapter( Graph delegate ) {
      this.delegate = delegate;
    }

    @Override
    public VertexAdapter getVertex( Object id ) {
      final Vertex vertex = delegate.getVertex( id );
      return vertex == null ? null : wrap( vertex );
    }

    @Override
    public VertexAdapter addVertex( Object id ) {
      return wrap( delegate.addVertex( id ) );
    }

    @Override
    public EdgeAdapter getEdge( Object id ) {
      final Edge edge = delegate.getEdge( id );
      return edge == null ? null : wrap( edge );
    }

    @Override
    public EdgeAdapter addEdge( Object id, VertexAdapter outVertex, VertexAdapter inVertex, String label ) {
      return wrap( delegate.addEdge( id, unwrap( outVertex ), unwrap( inVertex ), label ) );
    }

    @Override
    public Iterable<VertexAdapter> getVertices() {
      final List<VertexAdapter> vertices = new ArrayList<VertexAdapter>();
      for ( Vertex vertex : delegate.getVertices() ) {
        vertices.add( wrap( vertex ) );
      }
      return vertices;
    }
  }

  private static class BlueprintsVertexAdapter implements VertexAdapter {
    private final Vertex delegate;

    private BlueprintsVertexAdapter( Vertex delegate ) {
      this.delegate = delegate;
    }

    private Vertex getDelegate() {
      return delegate;
    }

    @Override
    public Object getId() {
      return delegate.getId();
    }

    @Override
    public <T> T getProperty( String key ) {
      return delegate.getProperty( key );
    }

    @Override
    public void setProperty( String key, Object value ) {
      delegate.setProperty( key, value );
    }

    @Override
    public java.util.Set<String> getPropertyKeys() {
      return delegate.getPropertyKeys();
    }

    @Override
    public Iterable<EdgeAdapter> getEdges( DirectionAdapter direction, String... labels ) {
      final List<EdgeAdapter> edges = new ArrayList<EdgeAdapter>();
      for ( Edge edge : delegate.getEdges( unwrap( direction ), labels ) ) {
        edges.add( wrap( edge ) );
      }
      return edges;
    }

    @Override
    public void remove() {
      delegate.remove();
    }

    @Override
    public boolean equals( Object obj ) {
      if ( this == obj ) {
        return true;
      }
      if ( !( obj instanceof BlueprintsVertexAdapter ) ) {
        return false;
      }
      return delegate.equals( ( (BlueprintsVertexAdapter) obj ).delegate );
    }

    @Override
    public int hashCode() {
      return delegate.hashCode();
    }
  }

  private static class BlueprintsEdgeAdapter implements EdgeAdapter {
    private final Edge delegate;

    private BlueprintsEdgeAdapter( Edge delegate ) {
      this.delegate = delegate;
    }

    private Edge getDelegate() {
      return delegate;
    }

    @Override
    public Object getId() {
      return delegate.getId();
    }

    @Override
    public String getLabel() {
      return delegate.getLabel();
    }

    @Override
    public VertexAdapter getVertex( DirectionAdapter direction ) {
      return wrap( delegate.getVertex( unwrap( direction ) ) );
    }

    @Override
    public void setProperty( String key, Object value ) {
      delegate.setProperty( key, value );
    }

    @Override
    public void remove() {
      delegate.remove();
    }

    @Override
    public boolean equals( Object obj ) {
      if ( this == obj ) {
        return true;
      }
      if ( !( obj instanceof BlueprintsEdgeAdapter ) ) {
        return false;
      }
      return delegate.equals( ( (BlueprintsEdgeAdapter) obj ).delegate );
    }

    @Override
    public int hashCode() {
      return delegate.hashCode();
    }
  }
}

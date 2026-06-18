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


package org.pentaho.metaverse.frames;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.pentaho.dictionary.DictionaryConst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * User: RFellows Date: 9/4/14
 */
public abstract class FramedMetaverseNode {
  protected final Vertex vertex;
  protected final Graph graph;

  protected FramedMetaverseNode( Vertex vertex, Graph graph ) {
    this.vertex = vertex;
    this.graph = graph;
  }

  public Vertex getV() {
    return vertex;
  }

  public Object getId() {
    return vertex.id();
  }

  public String getName() {
    return getStringValue( DictionaryConst.PROPERTY_NAME );
  }

  public String getType() {
    return getStringValue( DictionaryConst.PROPERTY_TYPE );
  }

  public Boolean isVirtual() {
    return getBooleanValue( "virtual" );
  }

  public String getDescription() {
    return getStringValue( DictionaryConst.PROPERTY_DESCRIPTION );
  }

  public List<Concept> getContainedNodes() {
    return wrapAsConcept( vertex.vertices( Direction.OUT, "contains" ) );
  }

  public List<TransformationNode> getExecutesNodes() {
    return wrapAs( vertex.vertices( Direction.OUT, "executes" ), v -> new TransformationNode( v, graph ) );
  }

  public List<Concept> getNodesPopulatedByMe() {
    return wrapAsConcept( vertex.vertices( Direction.OUT, "populates" ) );
  }

  public List<Concept> getConcreteNodes() {
    return wrapAsConcept( vertex.vertices( Direction.OUT, "typeconcept" ) );
  }

  public List<Concept> getOutNodes( String linkType ) {
    return wrapAsConcept( vertex.vertices( Direction.OUT, linkType ) );
  }

  public List<Concept> getInNodes( String linkType ) {
    return wrapAsConcept( vertex.vertices( Direction.IN, linkType ) );
  }

  public List<Concept> getAllOutNodes() {
    return wrapAsConcept( vertex.vertices( Direction.OUT ) );
  }

  public List<Concept> getAllInNodes() {
    return wrapAsConcept( vertex.vertices( Direction.IN ) );
  }

  public Object getProperty( final String propertyName ) {
    return vertex.property( propertyName ).isPresent() ? vertex.value( propertyName ) : null;
  }

  public Set<String> getPropertyNames() {
    return vertex.keys();
  }

  protected String getStringValue( String key ) {
    return vertex.property( key ).isPresent() ? vertex.<String>value( key ) : null;
  }

  protected Boolean getBooleanValue( String key ) {
    return vertex.property( key ).isPresent() ? vertex.<Boolean>value( key ) : null;
  }

  protected Integer getIntegerValue( String key ) {
    return vertex.property( key ).isPresent() ? vertex.<Integer>value( key ) : null;
  }

  @SuppressWarnings( "unchecked" )
  protected List<String> getStringListValue( String key ) {
    Object value = getProperty( key );
    if ( value == null ) {
      return Collections.emptyList();
    }
    if ( value instanceof List ) {
      return (List<String>) value;
    }
    return Collections.singletonList( value.toString() );
  }

  protected List<Concept> wrapAsConcept( Iterator<Vertex> it ) {
    return wrapAs( it, v -> new Concept( v, graph ) );
  }

  protected List<FramedMetaverseNode> wrapAsNodes( Iterator<Vertex> it ) {
    return wrapAs( it, v -> new Concept( v, graph ) );
  }

  protected <T> List<T> wrapAs( Iterator<Vertex> it, Function<Vertex, T> factory ) {
    List<T> list = new ArrayList<>();
    while ( it.hasNext() ) {
      list.add( factory.apply( it.next() ) );
    }
    return list;
  }

  protected Vertex firstOrNull( Iterator<Vertex> it ) {
    return it.hasNext() ? it.next() : null;
  }

  protected <T> T wrapSingle( Iterator<Vertex> it, Function<Vertex, T> factory ) {
    Vertex v = firstOrNull( it );
    return v == null ? null : factory.apply( v );
  }

  protected FramedMetaverseNode wrapNode( Vertex v ) {
    return v == null ? null : new Concept( v, graph );
  }

  @Override public boolean equals( Object other ) {
    if ( this == other ) {
      return true;
    }
    if ( !( other instanceof FramedMetaverseNode ) ) {
      return false;
    }
    FramedMetaverseNode that = (FramedMetaverseNode) other;
    return Objects.equals( getId(), that.getId() );
  }

  @Override public int hashCode() {
    return Objects.hashCode( getId() );
  }

  @Override public String toString() {
    return vertex.toString();
  }
}

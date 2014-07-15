package com.pentaho.metaverse.impl;

import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Metaverse Link
 */
public class MetaverseLink implements IMetaverseLink {

  /**
   * Property key for Link label
   */
  public static final String LABEL = "label";

  private IMetaverseNode fromNode;
  private IMetaverseNode toNode;
  private Map<String, Object> props = new HashMap<String, Object>( );

  /**
   * Default constructor
   */
  public MetaverseLink() {

  }

  /**
   * Constructor for creating a link with a single call
   * @param fromNode from node
   * @param label label for the link
   * @param toNode to node
   */
  public MetaverseLink( IMetaverseNode fromNode, String label, IMetaverseNode toNode ) {
    setFromNode( fromNode );
    setToNode( toNode );
    setLabel( label );
  }

  @Override
  public IMetaverseNode getFromNode() {
    return fromNode;
  }

  @Override
  public IMetaverseNode getToNode() {
    return toNode;
  }

  @Override
  public String getLabel() {
    if ( props.containsKey( LABEL ) ) {
      return props.get( LABEL ).toString();
    }
    return null;
  }

  @Override
  public void setFromNode( IMetaverseNode fromNode ) {
    this.fromNode = fromNode;
  }

  @Override
  public void setToNode( IMetaverseNode toNode ) {
    this.toNode = toNode;
  }

  @Override
  public void setLabel( String label ) {
    props.put( LABEL, label );
  }

  @Override public <T> T getProperty( String key ) {
    return (T) props.get( key );
  }

  @Override public Set<String> getPropertyKeys() {
    return props.keySet();
  }

  @Override public void setProperty( String key, Object value ) {
    props.put( key, value );
  }

  @Override public <T> T removeProperty( String key ) {
    if ( props.containsKey( key ) ) {
      T obj = (T) props.get( key );
      props.remove( key );
      return obj;
    } else {
      return null;
    }
  }
}

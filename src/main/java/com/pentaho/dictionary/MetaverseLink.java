package com.pentaho.dictionary;

import com.pentaho.metaverse.impl.PropertiesHolder;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

/**
 * Metaverse Link
 */
@SuppressWarnings( "rawtypes" )
public class MetaverseLink extends PropertiesHolder implements IMetaverseLink {

  /**
   * Property key for Link label
   */
  public static final String LABEL = "label";

  private IMetaverseNode fromNode;
  private IMetaverseNode toNode;
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
    if ( containsKey( LABEL ) ) {
      return getPropertyAsString( LABEL ).toString();
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
    properties.put( LABEL, label );
  }
}

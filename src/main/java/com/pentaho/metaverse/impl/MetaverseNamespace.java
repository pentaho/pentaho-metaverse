package com.pentaho.metaverse.impl;

import com.pentaho.dictionary.DictionaryHelper;
import com.pentaho.metaverse.api.INamespaceFactory;
import org.pentaho.platform.api.metaverse.INamespace;

/**
 * Created by gmoran on 8/7/14.
 */
public class MetaverseNamespace implements INamespace {

  private INamespace parent;

  private String namespace;

  private String fullyQualifiedNamespace;

  private String concatenationCharacter = DictionaryHelper.SEPARATOR;

  private INamespaceFactory factory;

  public MetaverseNamespace( INamespace parent, String namespace, INamespaceFactory factory) {

    this.parent = parent;
    this.namespace = namespace;
    this.factory = factory;

  }

  @Override
  public String getNamespaceId() {

    if ( fullyQualifiedNamespace != null ) {
      return fullyQualifiedNamespace;
    }

    if ( parent != null ) {
      fullyQualifiedNamespace = parent.getNamespaceId().concat( concatenationCharacter ).concat( namespace );
    } else {
      fullyQualifiedNamespace = namespace;
    }

    return fullyQualifiedNamespace;
  }

  @Override
  public INamespace getParentNamespace() {
    return parent;
  }

  /**
   * TODO: Need to add this to the platform API
   * @param child the name of the new descendant namespace, relative to the parent (this)
   * @return a new namespace
   */
  public INamespace getChildNamespace( String child ){
    return factory.createNameSpace( this, child );
  }

}

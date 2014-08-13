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

  private String type;

  public MetaverseNamespace( INamespace parent, String namespace, String type, INamespaceFactory factory ) {

    this.parent = parent;
    this.namespace = namespace;
    this.type = type;
    this.factory = factory;
  }

  public MetaverseNamespace( INamespace parent, String type, String namespace ) {

    this( parent, namespace, type, new NamespaceFactory() );

  }

  @Override
  public String getNamespaceId() {

    if ( fullyQualifiedNamespace != null ) {
      return fullyQualifiedNamespace;
    }

    if ( namespace == null ) {
      namespace = "";
    }

    if ( parent != null ) {
      fullyQualifiedNamespace = parent.getNamespaceId()
          .concat( concatenationCharacter )
          .concat( namespace );
    } else {
      fullyQualifiedNamespace = namespace;
    }
    if ( type != null ) {
      fullyQualifiedNamespace = fullyQualifiedNamespace.concat( concatenationCharacter ).concat( type );
    }

    return fullyQualifiedNamespace;
  }

  @Override
  public INamespace getParentNamespace() {
    return parent;
  }

  /**
   * @param child the name of the new descendant namespace, relative to the parent (this)
   * @return a new namespace
   */
  public INamespace getChildNamespace( String child, String type ) {
    return factory.createNameSpace( this, child, type );
  }

}

package com.pentaho.metaverse.impl;

import com.pentaho.dictionary.DictionaryHelper;
import org.pentaho.platform.api.metaverse.INamespace;

/**
 * Created by gmoran on 8/7/14.
 */
public class MetaverseNamespace implements INamespace {

  private INamespace parent;

  private String namespace;

  private String fullyQualifiedNamespace;

  private String concatenationCharacter = DictionaryHelper.SEPARATOR;

  public MetaverseNamespace( INamespace parent, String namespace ) {

    this.parent = parent;
    this.namespace = namespace;

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
}

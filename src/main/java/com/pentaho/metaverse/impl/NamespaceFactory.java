package com.pentaho.metaverse.impl;

import com.pentaho.metaverse.api.INamespaceFactory;
import org.pentaho.platform.api.metaverse.INamespace;

/**
 * Created by gmoran on 8/7/14.
 */
public class NamespaceFactory implements INamespaceFactory {

  @Override
  public INamespace createNameSpace( INamespace parent, String name, String type ) {
    return new MetaverseNamespace( parent, name, type, this );
  }
}

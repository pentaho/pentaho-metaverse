package com.pentaho.metaverse.api;

import org.pentaho.platform.api.metaverse.INamespace;

/**
 * Created by gmoran on 8/7/14.
 *
 * A factory to retrieve namespace builders from.
 */
public interface INamespaceFactory {

  /**
   * Creates a new namespace object
   * @param parent The namespace container one level above current
   * @param name the additional container identifier for this namespace
   * @param type the type of the object
   * @return INamespace object to manage namespace hierarchy
   */
  INamespace createNameSpace( INamespace parent, String name, String type );
}

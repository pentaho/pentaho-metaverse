package com.pentaho.metaverse.impl;

import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.INamespace;

/**
 * Created by mburgess on 8/12/14.
 */
public class MetaverseComponentDescriptor implements IMetaverseComponentDescriptor {

  private String name;
  private String type;
  private INamespace namespace;

  public MetaverseComponentDescriptor( String name, String type, INamespace namespace ) {
    this.name = name;
    this.type = type;
    this.namespace = namespace;
  }

  /**
   * Gets the name of this entity.
   *
   * @return the String name of the entity
   */
  @Override public String getName() {
    return name;
  }

  /**
   * Gets the metaverse-unique identifier for this entity.
   *
   * @return the String ID of the entity.
   */
  @Override public String getStringID() {
    return getNamespaceId();
  }

  /**
   * Gets the type of this entity.
   *
   * @return the String type of the entity
   */
  @Override public String getType() {
    return type;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  @Override public void setName( String name ) {
    this.name = name;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  @Override public void setType( String type ) {
    this.type = type;
  }

  /**
   * The entity namespace
   *
   * @return the namespace id, represents the container for this element
   */
  @Override public String getNamespaceId() {
    return namespace.getNamespaceId();
  }

  /**
   * @return the INamespace of the entity one level above the current
   */
  @Override public INamespace getParentNamespace() {
    return namespace.getParentNamespace();
  }

  /**
   * get the name space for the current level entity
   *
   * @param child the string representation of hte current entity's contribution to the namespace path
   * @return the namespace object for the entity represented by child
   */
  @Override public INamespace getChildNamespace( String child, String type ) {
    return namespace.getChildNamespace( child, type );
  }

  @Override public void setNamespace( INamespace namespace ) {
    this.namespace = namespace;
  }

  @Override public INamespace getNamespace() {
    return namespace;
  }
}

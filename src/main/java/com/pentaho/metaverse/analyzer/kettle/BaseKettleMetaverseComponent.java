/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */
package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.dictionary.DictionaryHelper;
import org.pentaho.platform.api.metaverse.IIdentifierModifiable;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.IRequiresMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IRequiresNamespace;

import java.io.Serializable;

public abstract class BaseKettleMetaverseComponent
    implements IRequiresMetaverseBuilder, IRequiresNamespace, IIdentifierModifiable, Serializable {

  private static final long serialVersionUID = 8122643311387257050L;

  /**
   * A reference to the metaverse builder.
   */
  protected IMetaverseBuilder metaverseBuilder;

  /**
   * A reference to the metaverse object factory.
   */
  protected IMetaverseObjectFactory metaverseObjectFactory;

  /**
   * A reference to this component's namespace
   */
  protected INamespace namespace = null;

  protected String id;

  protected String name;

  protected String type;

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.platform.api.metaverse.IDocumentAnalyzer#setMetaverseBuilder(org.pentaho.platform.api.metaverse.
   * IMetaverseBuilder)
   */
  @Override
  public void setMetaverseBuilder( IMetaverseBuilder metaverseBuilder ) {
    this.metaverseBuilder = metaverseBuilder;
    if ( metaverseBuilder != null ) {
      this.metaverseObjectFactory = metaverseBuilder.getMetaverseObjectFactory();
    }
  }

  protected IMetaverseBuilder getMetaverseBuilder() {
    return metaverseBuilder;
  }

  /**
   * Sets the namespace for this component
   *
   * @param ns the namespace to set
   */
  @Override
  public void setNamespace( INamespace ns ) {
    namespace = ns;
  }

  /**
   * Gets the namespace for this component
   *
   * @return the namespace for this component
   */
  public INamespace getNamespace() {
    return namespace;
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
    if ( id == null && name != null && namespace != null ) {
      id = namespace.getNamespaceId() + DictionaryHelper.SEPARATOR + name;
    }
    return id;
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
   * Sets the string id.
   *
   * @param id the new string id
   */
  @Override public void setStringID( String id ) {
    this.id = id;
  }
}

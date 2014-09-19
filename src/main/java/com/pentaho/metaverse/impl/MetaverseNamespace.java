/*
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

    if ( ( parent != null ) && ( parent.getNamespaceId() != null ) ) {
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

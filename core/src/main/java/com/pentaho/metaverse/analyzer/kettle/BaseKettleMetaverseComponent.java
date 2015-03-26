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

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.IMetaverseObjectFactory;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.IRequiresMetaverseBuilder;
import com.pentaho.metaverse.api.ILogicalIdGenerator;
import com.pentaho.metaverse.api.MetaverseException;

import java.io.Serializable;
import java.util.UUID;

public abstract class BaseKettleMetaverseComponent implements IRequiresMetaverseBuilder, Serializable {

  private static final long serialVersionUID = 8122643311387257050L;

  /**
   * A reference to the metaverse builder.
   */
  protected IMetaverseBuilder metaverseBuilder;

  /**
   * A reference to the metaverse object factory.
   */
  protected IMetaverseObjectFactory metaverseObjectFactory;

  /*
   * (non-Javadoc)
   *
   * @see com.pentaho.metaverse.api.IDocumentAnalyzer#setMetaverseBuilder(com.pentaho.metaverse.api.
   * IMetaverseBuilder)
   */
  @Override
  public void setMetaverseBuilder( IMetaverseBuilder metaverseBuilder ) {
    this.metaverseBuilder = metaverseBuilder;
    if ( metaverseBuilder != null ) {
      this.metaverseObjectFactory = metaverseBuilder.getMetaverseObjectFactory();
    }
  }

  public IMetaverseBuilder getMetaverseBuilder() {
    return metaverseBuilder;
  }

  protected INamespace getSiblingNamespace( INamespace namespace, String siblingName, String siblingType ) {
    if ( namespace == null ) {
      return null;
    }
    return namespace.getSiblingNamespace( siblingName, siblingType );
  }

  protected IMetaverseNode createNodeFromDescriptor( IComponentDescriptor descriptor ) {
    return createNodeFromDescriptor( descriptor, getLogicalIdGenerator() );
  }

  protected IMetaverseNode createNodeFromDescriptor(
      IComponentDescriptor descriptor, ILogicalIdGenerator idGenerator ) {

    String uuid = UUID.randomUUID().toString();

    IMetaverseNode node = null;
    if ( descriptor != null ) {
      node = metaverseObjectFactory.createNodeObject(
          uuid,
          descriptor.getName(),
          descriptor.getType() );

      if ( idGenerator.getLogicalIdPropertyKeys().contains( DictionaryConst.PROPERTY_NAMESPACE )
          && descriptor.getParentNamespace() != null ) {
        node.setProperty( DictionaryConst.PROPERTY_NAMESPACE, descriptor.getNamespace().getNamespaceId() );
      }
      node.setLogicalIdGenerator( idGenerator );
    }
    return node;
  }

  protected IMetaverseNode createFileNode( String fileName, IComponentDescriptor descriptor )
    throws MetaverseException {

    String normalized = KettleAnalyzerUtil.normalizeFilePath( fileName );
    IMetaverseNode fileNode = null;

    INamespace ns = descriptor.getNamespace();
    INamespace parentNs = ns.getParentNamespace();

    if ( descriptor != null ) {

      fileNode = metaverseObjectFactory.createNodeObject(
          parentNs == null ? ns : parentNs,
          normalized,
          DictionaryConst.NODE_TYPE_FILE );
    }

    fileNode.setProperty( DictionaryConst.PROPERTY_PATH, normalized );
    fileNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_FILE );

    return fileNode;
  }

  protected ILogicalIdGenerator getLogicalIdGenerator() {
    return DictionaryConst.LOGICAL_ID_GENERATOR_DEFAULT;
  }
}

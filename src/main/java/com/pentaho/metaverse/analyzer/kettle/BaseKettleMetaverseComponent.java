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
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IAnalysisContext;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.IRequiresMetaverseBuilder;
import org.pentaho.platform.api.metaverse.MetaverseException;

import java.io.Serializable;

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

  protected IMetaverseComponentDescriptor getChildComponentDescriptor(
      IMetaverseComponentDescriptor parentDescriptor, String name, String type, IAnalysisContext context ) {
    return new MetaverseComponentDescriptor( name, type,
        parentDescriptor == null ? null : parentDescriptor.getChildNamespace( name, type ),
        context );
  }

  protected IMetaverseComponentDescriptor getChildComponentDescriptor(
      INamespace namespace, String name, String type, IAnalysisContext context ) {
    IMetaverseComponentDescriptor mcd = new MetaverseComponentDescriptor( name, type, namespace, context );
    return getChildComponentDescriptor( mcd, name, type, context );
  }

  protected INamespace getSiblingNamespace( INamespace namespace, String siblingName, String siblingType ) {
    if ( namespace == null ) {
      return null;
    }
    INamespace parentNamespace = namespace.getParentNamespace();
    if ( parentNamespace == null ) {
      return null;
    } else {
      return parentNamespace.getChildNamespace( siblingName, siblingType );
    }
  }

  protected IMetaverseNode createNodeFromDescriptor( IMetaverseComponentDescriptor descriptor ) {
    return descriptor == null
        ? null
        : metaverseObjectFactory.createNodeObject(
          descriptor.getNamespaceId(),
          descriptor.getName(),
          descriptor.getType() );
  }

  protected IMetaverseNode createFileNode( String fileName, IMetaverseComponentDescriptor descriptor ) throws MetaverseException {
    String normalized = KettleAnalyzerUtil.normalizeFilePath( fileName );

    IMetaverseNode fileNode = createNodeFromDescriptor(
      getChildComponentDescriptor( descriptor, normalized, DictionaryConst.NODE_TYPE_FILE, descriptor.getContext() ) );

    fileNode.setProperty( DictionaryConst.PROPERTY_PATH, normalized );
    fileNode.setLogicalIdPropertyKeys( DictionaryConst.PROPERTY_PATH );

    return fileNode;
  }
}

/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.api.analyzer.kettle;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.ILogicalIdGenerator;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.IRequiresMetaverseBuilder;
import org.pentaho.metaverse.api.MetaverseException;

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
   * @see IDocumentAnalyzer#setMetaverseBuilder(com.pentaho.metaverse.api.
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

    String normalized;
    IMetaverseNode fileNode = null;

    if ( fileName != null && descriptor != null ) {
      normalized = KettleAnalyzerUtil.normalizeFilePath( fileName );

      INamespace ns = descriptor.getNamespace();
      INamespace parentNs = ns.getParentNamespace();

      fileNode = metaverseObjectFactory.createNodeObject(
        parentNs == null ? ns : parentNs,
        normalized,
        DictionaryConst.NODE_TYPE_FILE );

      fileNode.setProperty( DictionaryConst.PROPERTY_PATH, normalized );
      fileNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_FILE );
    }
    return fileNode;
  }

  protected ILogicalIdGenerator getLogicalIdGenerator() {
    return DictionaryConst.LOGICAL_ID_GENERATOR_DEFAULT;
  }
}

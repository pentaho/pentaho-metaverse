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

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.ILogicalIdGenerator;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.messages.Messages;

/**
 * DatabaseConnectionAnalyzer collects metadata about a PDI database connection
 */
public abstract class DatabaseConnectionAnalyzer<T> extends BaseKettleMetaverseComponent
    implements IDatabaseConnectionAnalyzer<T> {

  /**
   * Analyzes a database connection for metadata.
   *
   * @param dbMeta the object
   * @see IAnalyzer#analyze(IComponentDescriptor, java.lang.Object)
   */
  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, DatabaseMeta dbMeta )
    throws MetaverseAnalyzerException {

    if ( dbMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.DatabaseMeta.IsNull" ) );
    }

    if ( metaverseObjectFactory == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseObjectFactory.IsNull" ) );
    }

    if ( metaverseBuilder == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseBuilder.IsNull" ) );
    }

    IMetaverseNode node = createNodeFromDescriptor( descriptor );
    node.setType( DictionaryConst.NODE_TYPE_DATASOURCE );

    int accessType = dbMeta.getAccessType();
    node.setProperty( "accessType", accessType );

    String accessTypeDesc = dbMeta.getAccessTypeDesc();
    node.setProperty( "accessTypeDesc", accessTypeDesc );

    String databaseName = dbMeta.getDatabaseName();
    node.setProperty( "databaseName", databaseName );

    node.setProperty( "name", dbMeta.getName() );

    String port = dbMeta.getDatabasePortNumberString();
    node.setProperty( DictionaryConst.PROPERTY_PORT, port );

    String host = dbMeta.getHostname();
    node.setProperty( DictionaryConst.PROPERTY_HOST_NAME, host );

    String user = dbMeta.getUsername();
    node.setProperty( DictionaryConst.PROPERTY_USER_NAME, user );

    boolean shared = dbMeta.isShared();
    node.setProperty( "shared", shared );

    if ( accessTypeDesc != null && accessTypeDesc.equals( "JNDI" ) ) {
      node.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DB_JNDI );
    } else {
      node.setLogicalIdGenerator( getLogicalIdGenerator() );
    }

    metaverseBuilder.addNode( node );

    return node;

  }

  @Override
  protected ILogicalIdGenerator getLogicalIdGenerator() {
    return DictionaryConst.LOGICAL_ID_GENERATOR_DB_JDBC;
  }

  @Override
  public IComponentDescriptor buildComponentDescriptor( IComponentDescriptor parentDescriptor,
                                                        DatabaseMeta connection ) {

    IComponentDescriptor dbDescriptor =
        new MetaverseComponentDescriptor( connection.getName(), DictionaryConst.NODE_TYPE_DATASOURCE, parentDescriptor
            .getNamespace(), parentDescriptor.getContext() );

    return dbDescriptor;

  }

}

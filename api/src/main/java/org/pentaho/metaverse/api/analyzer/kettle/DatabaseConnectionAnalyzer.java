/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse.api.analyzer.kettle;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseInterface;
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
   * @see org.pentaho.metaverse.api.IAnalyzer#analyze(IComponentDescriptor, java.lang.Object)
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

    String accessTypeDesc = dbMeta.environmentSubstitute( dbMeta.getAccessTypeDesc() );
    node.setProperty( "accessTypeDesc", accessTypeDesc );

    String databaseName = dbMeta.environmentSubstitute( dbMeta.getDatabaseName() );
    node.setProperty( "databaseName", databaseName );

    String connectionName = dbMeta.environmentSubstitute( dbMeta.getName() );
    node.setProperty( "name", connectionName );

    DatabaseInterface dbInterface = dbMeta.getDatabaseInterface();
    node.setProperty( "databaseType",
      dbInterface != null
        ? Const.NVL( dbInterface.getPluginName(), "Unknown" )
        : "Unknown" );

    String port = dbMeta.environmentSubstitute( dbMeta.getDatabasePortNumberString() );
    node.setProperty( DictionaryConst.PROPERTY_PORT, port );

    String host = dbMeta.environmentSubstitute( dbMeta.getHostname() );
    node.setProperty( DictionaryConst.PROPERTY_HOST_NAME, host );

    String user = dbMeta.environmentSubstitute( dbMeta.getUsername() );
    node.setProperty( DictionaryConst.PROPERTY_USER_NAME, user );

    boolean shared = dbMeta.isShared();
    node.setProperty( "shared", shared );

    if ( accessTypeDesc != null && accessTypeDesc.equals( "JNDI" ) ) {
      node.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DB_JNDI );
    } else {
      node.setLogicalIdGenerator( getLogicalIdGenerator() );
    }

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

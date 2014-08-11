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
import com.pentaho.dictionary.DictionaryHelper;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;


/**
 * DatabaseConnectionAnalyzer collects metadata about a PDI database connection
 */
public class DatabaseConnectionAnalyzer extends BaseKettleMetaverseComponent implements IDatabaseConnectionAnalyzer {

  /**
   * Analyzes a database connection for metadata.
   * 
   * @param dbMeta
   *          the object
   * @see org.pentaho.platform.api.metaverse.IAnalyzer#analyze(java.lang.Object)
   */
  @Override
  public IMetaverseNode analyze( DatabaseMeta dbMeta ) throws MetaverseAnalyzerException {

    if ( dbMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.DatabaseMeta.IsNull" ) );
    }

    if ( metaverseObjectFactory == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseObjectFactory.IsNull" ) );
    }

    if ( metaverseBuilder == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseBuilder.IsNull" ) );
    }

    String type = DictionaryConst.NODE_TYPE_DATASOURCE;

    IMetaverseNode node = metaverseObjectFactory.createNodeObject(
        DictionaryHelper.getId( dbMeta.getClass(),
            getNamespace().getNamespaceId(), dbMeta.getName() ),
        dbMeta.getName(),
        type );

    int accessType = dbMeta.getAccessType();
    node.setProperty( "accessType", accessType );

    String accessTypeDesc = dbMeta.getAccessTypeDesc();
    node.setProperty( "accessTypeDesc", accessTypeDesc );

    String databaseName = dbMeta.getDatabaseName();
    node.setProperty( "databaseName", databaseName );

    String port = dbMeta.getDatabasePortNumberString();
    node.setProperty( "port", port );

    String host = dbMeta.getHostname();
    node.setProperty( "hostName", host );

    String user = dbMeta.getUsername();
    node.setProperty( "userName", user );

    String pass = dbMeta.getPassword();
    node.setProperty( "password", pass );

    boolean shared = dbMeta.isShared();
    node.setProperty( "shared", shared );

    // TODO If these attributes are important, we will need to
    // TODO account for the same attributes in partitions in clusters

    metaverseBuilder.addNode( node );

    return node;

  }

}

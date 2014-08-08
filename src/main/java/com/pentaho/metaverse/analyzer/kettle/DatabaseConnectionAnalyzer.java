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
   * @param object
   *          the object
   * @see org.pentaho.platform.api.metaverse.IAnalyzer#analyze(java.lang.Object)
   */
  @Override
  public IMetaverseNode analyze( DatabaseMeta object ) throws MetaverseAnalyzerException {

    if ( object == null ) {
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
        DictionaryHelper.getId( object.getClass(),
            getNamespace().getNamespaceId(), object.getName() ),
        object.getName(),
        type );

    int accessType = object.getAccessType();
    node.setProperty( "accessType", accessType );

    String accessTypeDesc = object.getAccessTypeDesc();
    node.setProperty( "accessTypeDesc", accessTypeDesc );

    String databaseName = object.getDatabaseName();
    node.setProperty( "databaseName", databaseName );

    String port = object.getDatabasePortNumberString();
    node.setProperty( "port", port );

    String host = object.getHostname();
    node.setProperty( "hostName", host );

    String user = object.getUsername();
    node.setProperty( "userName", user );

    String pass = object.getPassword();
    node.setProperty( "password", pass );

    boolean shared = object.isShared();
    node.setProperty( "shared", shared );

    // TODO If these attributes are important, we will need to
    // TODO account for the same attributes in partitions in clusters

    metaverseBuilder.addNode( node );

    return node;

  }

}

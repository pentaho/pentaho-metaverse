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

package com.pentaho.metaverse.impl.model;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.resource.ResourceEntry;

/**
 * ExternalResourceInfoFactory provides a number of utility methods to create IExternalResourceInfo objects out of
 * other objects (such as DatabaseMeta, ResourceEntry, FileObject, etc.)
 */
public class ExternalResourceInfoFactory {

  private static final boolean DEFAULT_IS_INPUT = true;

  protected ExternalResourceInfoFactory() {
    // Protected per Singleton pattern (but available for test/coverage)
  }

  public static IExternalResourceInfo createDatabaseResource( DatabaseMeta databaseMeta ) {
    return createDatabaseResource( databaseMeta, DEFAULT_IS_INPUT );
  }

  public static IExternalResourceInfo createDatabaseResource( DatabaseMeta databaseMeta, boolean isInput ) {
    BaseDatabaseResourceInfo resourceInfo;
    if ( "Native".equals( databaseMeta.getAccessTypeDesc() ) ) {
      resourceInfo = new JdbcResourceInfo( databaseMeta );
    } else {
      resourceInfo = new JndiResourceInfo( databaseMeta );
    }
    resourceInfo.setInput( isInput );
    return resourceInfo;
  }

  public static IExternalResourceInfo createResource( ResourceEntry resourceEntry ) {
    return createResource( resourceEntry, DEFAULT_IS_INPUT );
  }

  public static IExternalResourceInfo createResource( ResourceEntry resourceEntry, boolean isInput ) {
    BaseResourceInfo resourceInfo = new BaseResourceInfo();
    resourceInfo.setName( resourceEntry.getResource() );
    resourceInfo.setInput( isInput );
    switch ( resourceEntry.getResourcetype() ) {
      case ACTIONFILE:
      case FILE:
        resourceInfo.setType( DictionaryConst.NODE_TYPE_FILE );
        break;
      case URL:
        resourceInfo.setType( DictionaryConst.NODE_TYPE_WEBSERVICE );
        break;
      case CONNECTION:
      case DATABASENAME:
        resourceInfo.setType( DictionaryConst.NODE_TYPE_DATASOURCE );
        break;
      case SERVER:
        resourceInfo.setType( "SERVER" );
        break;
      case OTHER:
      default:
        resourceInfo.setType( "OTHER" );
    }
    return resourceInfo;
  }

  public static IExternalResourceInfo createFileResource( FileObject fileObject ) {
    return createFileResource( fileObject, DEFAULT_IS_INPUT );
  }

  public static IExternalResourceInfo createFileResource( FileObject fileObject, boolean isInput ) {
    BaseResourceInfo resource = null;
    if ( fileObject != null ) {
      resource = new BaseResourceInfo();
      resource.setName( fileObject.getName().getPath() );
      resource.setInput( isInput );
      resource.setType( DictionaryConst.NODE_TYPE_FILE );
    }
    return resource;
  }
}

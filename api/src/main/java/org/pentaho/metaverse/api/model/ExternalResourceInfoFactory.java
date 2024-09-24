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

package org.pentaho.metaverse.api.model;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.dictionary.DictionaryConst;

/**
 * ExternalResourceInfoFactory provides a number of utility methods to create IExternalResourceInfo objects out of other
 * objects (such as DatabaseMeta, ResourceEntry, FileObject, etc.)
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
    } else if ( "OCI".equals( databaseMeta.getAccessTypeDesc() ) ) {
      resourceInfo = new OCIResourceInfo( databaseMeta );
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
        resourceInfo = (WebServiceResourceInfo) createURLResource( resourceEntry.getResource(), isInput );
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
      resource.setName( fileObject.getPublicURIString() );
      resource.setInput( isInput );
      // default value, different value can be specified by the custom analyzer
      resource.setType( DictionaryConst.NODE_TYPE_FILE );
    }
    return resource;
  }

  public static IExternalResourceInfo createURLResource( String url ) {
    return createURLResource( url, DEFAULT_IS_INPUT );
  }

  public static IExternalResourceInfo createURLResource( String url, boolean isInput ) {
    WebServiceResourceInfo resource = null;
    if ( url != null ) {
      resource = new WebServiceResourceInfo();
      resource.setName( url );
      resource.setInput( isInput );
    }
    return resource;
  }
}

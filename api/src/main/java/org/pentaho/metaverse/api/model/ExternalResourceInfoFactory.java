/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
      resource.setName( fileObject.getName().getPath() );
      resource.setInput( isInput );
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

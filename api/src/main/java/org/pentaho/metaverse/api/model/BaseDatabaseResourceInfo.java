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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.dictionary.DictionaryConst;

/**
 * User: RFellows Date: 12/5/14
 */
public class BaseDatabaseResourceInfo extends BaseResourceInfo implements IExternalResourceInfo {
  private String pluginId;

  public BaseDatabaseResourceInfo() {
  }

  public BaseDatabaseResourceInfo( DatabaseMeta databaseMeta ) {
    setName( databaseMeta.getName() );
    setDescription( databaseMeta.getDescription() );
    setPluginId( databaseMeta.getDatabaseInterface().getPluginId() );
  }

  @JsonProperty( DictionaryConst.PROPERTY_PLUGIN_ID )
  public String getPluginId() {
    return pluginId;
  }

  public void setPluginId( String pluginId ) {
    this.pluginId = pluginId;
  }

}

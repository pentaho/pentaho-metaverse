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

import org.pentaho.di.core.database.DatabaseMeta;

public class JndiResourceInfo extends BaseDatabaseResourceInfo implements IExternalResourceInfo {

  public static final String JNDI = "JNDI";

  @Override public String getType() {
    return JNDI;
  }

  public JndiResourceInfo( DatabaseMeta databaseMeta ) {
    super( databaseMeta );
    if ( !getType().equals( databaseMeta.getAccessTypeDesc() ) ) {
      throw new IllegalArgumentException( "DatabaseMeta is not JNDI, it is " + databaseMeta.getAccessTypeDesc() );
    }
  }

  public JndiResourceInfo() {
  }

  public JndiResourceInfo( String name ) {
    setName( name );
  }
}

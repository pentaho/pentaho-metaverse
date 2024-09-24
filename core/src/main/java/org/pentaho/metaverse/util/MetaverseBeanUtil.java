/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.util;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MetaverseBeanUtil is a type of object factory that allows users to get blueprint-injected instances of objects
 */
public class MetaverseBeanUtil {

  private static MetaverseBeanUtil INSTANCE = new MetaverseBeanUtil();
  private Logger logger = LoggerFactory.getLogger( MetaverseBeanUtil.class );

  private MetaverseBeanUtil() {
    // private for singleton pattern
  }

  public static MetaverseBeanUtil getInstance() {
    return INSTANCE;
  }

  public Object get( String id ) {
    Object service = null;
    try {
      service = PentahoSystem.get( Class.forName( id ) );
    } catch ( ClassNotFoundException e ) {
      logger.error( "Error getting service", e );
    }
    return service;
  }

  public Object get( Class clazz ) {
    return PentahoSystem.get( clazz );
  }

}

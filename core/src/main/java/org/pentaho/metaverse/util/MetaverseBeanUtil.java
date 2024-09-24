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

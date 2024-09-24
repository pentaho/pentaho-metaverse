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

package org.pentaho.metaverse.api.model.kettle;

public enum MetaverseExtensionPoint {

  TransLineageWriteEnd( "TransLineageWriteEnd", "GraphML Lineage has been written" ),
  JobLineageWriteEnd( "JobLineageWriteEnd", "GraphML Lineage has been written" );

  public String id;

  public String description;

  private MetaverseExtensionPoint( String id, String description ) {
    this.id = id;
    this.description = description;
  }
}

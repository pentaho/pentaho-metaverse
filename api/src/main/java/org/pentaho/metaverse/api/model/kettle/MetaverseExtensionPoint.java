/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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

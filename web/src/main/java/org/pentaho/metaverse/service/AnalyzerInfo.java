/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/



package org.pentaho.metaverse.service;

public class AnalyzerInfo {
  private String meta;
  public AnalyzerInfo() {
  }

  public AnalyzerInfo( String stepMeta ) {
    this.meta = stepMeta;
  }

  public String getMeta() {
    return meta;
  }

  public void setMeta( String meta ) {
    this.meta = meta;
  }
}

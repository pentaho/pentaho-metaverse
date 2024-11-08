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

import org.pentaho.metaverse.api.ILineageCollector;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class LineageStreamingOutput implements StreamingOutput {
  private List<String> artifacts;
  private ILineageCollector lineageCollector;

  public LineageStreamingOutput( List<String> artifacts, ILineageCollector lineageCollector ) {
    this.artifacts = artifacts;
    this.lineageCollector = lineageCollector;
  }

  @Override
  public void write( OutputStream outputStream ) throws IOException, WebApplicationException {
    lineageCollector.compressArtifacts( artifacts, outputStream );
    outputStream.flush();
  }
}

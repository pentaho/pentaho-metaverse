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

package org.pentaho.metaverse.service;

import org.pentaho.metaverse.api.ILineageCollector;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
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

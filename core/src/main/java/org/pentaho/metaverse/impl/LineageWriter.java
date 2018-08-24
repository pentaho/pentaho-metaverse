/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.impl;

import org.pentaho.metaverse.api.IGraphWriter;
import org.pentaho.metaverse.api.ILineageWriter;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.impl.model.ExecutionProfileUtil;
import org.pentaho.metaverse.util.MetaverseUtil;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by mburgess on 3/26/15.
 */
public class LineageWriter implements ILineageWriter {

  private OutputStream profileOutputStream;
  private OutputStream graphOutputStream;
  private IGraphWriter graphWriter;
  private String outputStrategy = DEFAULT_OUTPUT_STRATEGY;

  @Override
  public void outputExecutionProfile( LineageHolder holder ) throws IOException {
    ExecutionProfileUtil.outputExecutionProfile( getProfileOutputStream(), holder.getExecutionProfile() );
  }

  @Override
  public void outputLineageGraph( LineageHolder holder ) throws IOException {
    if ( graphWriter != null ) {
      // no-op by default, can be used to introduce an artificial delay in the graphml file, for testing purposes
      MetaverseUtil.delay();
      graphWriter.outputGraph( holder.getMetaverseBuilder().getGraph(), getGraphOutputStream() );
      MetaverseUtil.delay();
    } else {
      throw new IOException( "No graph output stream associated with this LineageWriter" ); // TODO different exception?
    }
  }

  public OutputStream getProfileOutputStream() {
    if ( profileOutputStream == null ) {
      profileOutputStream = System.out;
    }
    return profileOutputStream;
  }

  public OutputStream getGraphOutputStream() {
    if ( graphOutputStream == null ) {
      return System.out;
    } else {
      return graphOutputStream;
    }
  }

  public void setProfileOutputStream( OutputStream profileOutputStream ) {
    this.profileOutputStream = profileOutputStream;
  }

  public void setGraphOutputStream( OutputStream graphOutputStream ) {
    this.graphOutputStream = graphOutputStream;
  }

  public IGraphWriter getGraphWriter() {
    return graphWriter;
  }

  public void setGraphWriter( IGraphWriter graphWriter ) {
    this.graphWriter = graphWriter;
  }

  /**
   * Returns the output strategy (all, latest, none, etc.) as a string
   *
   * @return The String name of the output strategy
   */
  @Override
  public String getOutputStrategy() {
    return outputStrategy;
  }

  /**
   * Sets the output strategy (all, latest, none) for this writer
   *
   * @param strategy The strategy to use when outputting lineage information
   */
  @Override
  public void setOutputStrategy( String strategy ) {
    this.outputStrategy = strategy;
  }

  /**
   * Method called on the writer to do any cleanup of the output artifacts, folders, etc.
   *
   * @param holder Context of the lineage related info
   */
  @Override
  public void cleanOutput( LineageHolder holder ) {
    // Nothing to do here
  }


}

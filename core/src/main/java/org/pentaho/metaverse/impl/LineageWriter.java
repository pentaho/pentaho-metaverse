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

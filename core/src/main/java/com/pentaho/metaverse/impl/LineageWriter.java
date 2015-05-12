/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.metaverse.impl;

import com.pentaho.metaverse.api.IGraphWriter;
import com.pentaho.metaverse.api.ILineageWriter;
import com.pentaho.metaverse.api.model.LineageHolder;
import com.pentaho.metaverse.impl.model.ExecutionProfileUtil;

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
      graphWriter.outputGraph( holder.getMetaverseBuilder().getGraph(), getGraphOutputStream() );
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

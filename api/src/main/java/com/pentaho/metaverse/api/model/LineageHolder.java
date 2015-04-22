/*!
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

package com.pentaho.metaverse.api.model;

import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IRequiresMetaverseBuilder;

import java.util.concurrent.Future;

/**
 * LineageHolder contains objects needed by various utilities to perform lineage operations. These objects include an
 * execution profile and a metaverse builder. The two can be combined or used separately to export execution profiles
 * or graphs, respectively.
 */
public class LineageHolder implements IRequiresMetaverseBuilder {

  private String id;

  private IExecutionProfile executionProfile;

  private IMetaverseBuilder metaverseBuilder;

  private Future lineageTask;


  public LineageHolder() {
  }

  public LineageHolder( IExecutionProfile profile, IMetaverseBuilder builder ) {
    this();
    setExecutionProfile( profile );
    setMetaverseBuilder( builder );
  }

  public IExecutionProfile getExecutionProfile() {
    return executionProfile;
  }

  public void setExecutionProfile( IExecutionProfile executionProfile ) {
    this.executionProfile = executionProfile;
  }

  /**
   * Gets the metaverse builder.
   *
   * @return the metaverse builder
   */
  @Override
  public IMetaverseBuilder getMetaverseBuilder() {
    return metaverseBuilder;
  }

  /**
   * Sets the metaverse builder.
   *
   * @param metaverseBuilder the new metaverse builder
   */
  @Override
  public void setMetaverseBuilder( IMetaverseBuilder metaverseBuilder ) {
    this.metaverseBuilder = metaverseBuilder;
  }


  public Future getLineageTask() {
    return lineageTask;
  }

  public void setLineageTask( Future lineageTask ) {
    this.lineageTask = lineageTask;
  }

  public String getId() {
    if ( id == null && executionProfile != null ) {
      id = executionProfile.getPath();
    }
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }
}

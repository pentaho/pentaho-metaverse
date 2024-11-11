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


package org.pentaho.metaverse.api.model;

import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IRequiresMetaverseBuilder;
import org.pentaho.metaverse.api.messages.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * LineageHolder contains objects needed by various utilities to perform lineage operations. These objects include an
 * execution profile and a metaverse builder. The two can be combined or used separately to export execution profiles
 * or graphs, respectively.
 */
public class LineageHolder implements IRequiresMetaverseBuilder {

  private String id;

  private List<Object> subTransAndJobs = new ArrayList();

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

  public void addSubTransOrJob( final Object executable ) {
    if ( executable instanceof Trans || executable instanceof Job ) {
      this.subTransAndJobs.add( executable );
    } else {
      throw new IllegalArgumentException( Messages.getString( "ERROR.NotATransOrJob" ) );
    }
  }

  public List<Object> getSubTransAndJobs() {
    return this.subTransAndJobs;
  }

}

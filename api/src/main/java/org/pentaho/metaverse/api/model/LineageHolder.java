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

package org.pentaho.metaverse.api.model;

import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IRequiresMetaverseBuilder;

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

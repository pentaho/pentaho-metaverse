/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package org.pentaho.metaverse.impl.model;

import org.pentaho.metaverse.api.model.BaseInfo;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionEngine;
import org.pentaho.metaverse.api.model.IExecutionProfile;

public class ExecutionProfile extends BaseInfo implements IExecutionProfile {

  private String path;
  private String type;
  private IExecutionEngine executionEngine = new ExecutionEngine();
  private IExecutionData executionData = new ExecutionData();

  public ExecutionProfile() {
    super();
  }

  public ExecutionProfile( String name, String path, String type, String description ) {
    this();
    this.setName( name );
    this.setDescription( description );
    this.path = path;
    this.type = type;
  }

  /**
   * Returns the path to the artifact represented by this execution profile
   *
   * @return a string representing the path to the execution profile's associated artifact
   */
  @Override
  public String getPath() {
    return path;
  }

  /**
   * Sets the path for the artifact represented by this execution profile
   *
   * @param path the path to the artifact for this execution profile
   */
  @Override
  public void setPath( String path ) {
    this.path = path;
  }

  /**
   * Returns the type of the artifact represented by this execution profile
   *
   * @return a string representing the type of the execution profile's associated artifact
   */
  @Override
  public String getType() {
    return type;
  }

  /**
   * Sets the type of the artifact represented by this execution profile
   *
   * @param type the type of the artifact for this execution profile
   */
  @Override
  public void setType( String type ) {
    this.type = type;
  }

  /**
   * Returns the engine used to execute the associated artifact
   *
   * @return the execution engine associated with the execution profile's artifact
   */
  @Override
  public IExecutionEngine getExecutionEngine() {
    return executionEngine;
  }

  /**
   * Sets the engine used to execute the associated artifact
   *
   * @param executionEngine the engine associated with executing this execution profile's artifact
   */
  @Override
  public void setExecutionEngine( IExecutionEngine executionEngine ) {
    this.executionEngine = executionEngine;
  }

  /**
   * Returns the data associated with this execution of the artifact
   *
   * @return the execution data associated with the execution profile's artifact
   */
  @Override
  public IExecutionData getExecutionData() {
    return executionData;
  }

  /**
   * Sets the data associated with this execution of the artifact
   *
   * @param executionData the data associated with executing this execution profile's artifact
   */
  @Override
  public void setExecutionData( IExecutionData executionData ) {
    this.executionData = executionData;
  }

}

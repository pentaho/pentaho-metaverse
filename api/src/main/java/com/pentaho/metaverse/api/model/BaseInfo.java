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

package com.pentaho.metaverse.api.model;

public class BaseInfo implements IInfo {
  private String name;
  private String description;

  /**
   * Returns the name of this artifact
   *
   * @return a string representing the name of this artifact
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns a description of this artifact
   *
   * @return a string representing the description of this artifact
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Sets the name of this artifact
   *
   * @param name the name to set for the artifact
   */
  @Override
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * Sets the description of this artifact
   *
   * @param description the description to set for the artifact
   */
  @Override
  public void setDescription( String description ) {
    this.description = description;
  }
}

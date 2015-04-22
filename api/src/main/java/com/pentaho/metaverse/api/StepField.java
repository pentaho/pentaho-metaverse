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
package com.pentaho.metaverse.api;

import java.util.Collections;
import java.util.Map;

/**
 * A simple POJO (bean) that associates a transformation step with a field
 */
public class StepField {

  private String stepName;
  private String fieldName;

  public StepField() {
  }

  public StepField( String stepName, String fieldName ) {
    this();
    this.stepName = stepName;
    this.fieldName = fieldName;
  }

  // Copy Constructor
  public StepField( StepField stepField ) {
    this( stepField.getStepName(), stepField.getFieldName() );
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName( String fieldName ) {
    this.fieldName = fieldName;
  }

  public String getStepName() {
    return stepName;
  }

  public void setStepName( String stepName ) {
    this.stepName = stepName;
  }

  public Map<String, String> toMap() {
    return Collections.singletonMap( stepName, fieldName );
  }

  public String toString() {
    StringBuffer sb = new StringBuffer( "{ step:" );
    sb.append( stepName );
    sb.append( ", field:" );
    sb.append( fieldName );
    sb.append( " }" );
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = stepName.hashCode();
    result = 31 * result + fieldName.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object sf ) {
    return (
      sf instanceof StepField
        && getStepName().equals( ( (StepField) sf ).getStepName() )
        && getFieldName().equals( ( (StepField) sf ).getFieldName() )
        && hashCode() == sf.hashCode()
      );
  }
}

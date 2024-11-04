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


package org.pentaho.metaverse.api;

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

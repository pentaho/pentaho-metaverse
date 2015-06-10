/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

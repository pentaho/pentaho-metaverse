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

import org.pentaho.metaverse.api.model.Operations;

/**
 * A simple POJO (bean) that associates a transformation step with a field and the operation(s)
 * that were performed on the field.
 */
public class StepFieldOperations extends StepField {

  private Operations operations;

  public StepFieldOperations() {
    super();
  }

  public StepFieldOperations( StepField stepField, Operations operations ) {
    super( stepField );
    this.operations = operations;
  }

  public StepFieldOperations( String stepName, String fieldName, Operations operations ) {
    this( new StepField( stepName, fieldName ), operations );
  }

  public Operations getOperations() {
    return operations;
  }

  public void setOperations( Operations operations ) {
    this.operations = operations;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer( "{ step:" );
    sb.append( getStepName() );
    sb.append( ", field:" );
    sb.append( getFieldName() );
    sb.append( ", operations: " );
    sb.append( operations == null ? "{ none }" : operations );
    sb.append( " }" );
    return sb.toString();
  }
}

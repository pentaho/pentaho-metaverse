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

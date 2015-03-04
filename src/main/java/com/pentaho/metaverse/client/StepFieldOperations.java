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
package com.pentaho.metaverse.client;

import com.pentaho.metaverse.api.model.Operations;

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

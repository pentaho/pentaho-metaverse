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

package org.pentaho.metaverse.api.model.kettle;

/**
 * User: RFellows Date: 12/11/14
 */
public class FieldMapping implements IFieldMapping {

  private String sourceFieldName;
  private String targetFieldName;

  public FieldMapping() {
  }

  public FieldMapping( String sourceFieldName, String targetFieldName ) {
    this.sourceFieldName = sourceFieldName;
    this.targetFieldName = targetFieldName;
  }

  @Override
  public String getSourceFieldName() {
    return sourceFieldName;
  }

  @Override
  public String getTargetFieldName() {
    return targetFieldName;
  }

  public void setSourceFieldName( String sourceFieldName ) {
    this.sourceFieldName = sourceFieldName;
  }

  public void setTargetFieldName( String targetFieldName ) {
    this.targetFieldName = targetFieldName;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    FieldMapping that = (FieldMapping) o;

    if ( sourceFieldName != null ? !sourceFieldName.equals( that.sourceFieldName ) : that.sourceFieldName != null ) {
      return false;
    }
    if ( targetFieldName != null ? !targetFieldName.equals( that.targetFieldName ) : that.targetFieldName != null ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = sourceFieldName != null ? sourceFieldName.hashCode() : 0;
    result = 31 * result + ( targetFieldName != null ? targetFieldName.hashCode() : 0 );
    return result;
  }
}

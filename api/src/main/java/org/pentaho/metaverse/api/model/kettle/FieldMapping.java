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

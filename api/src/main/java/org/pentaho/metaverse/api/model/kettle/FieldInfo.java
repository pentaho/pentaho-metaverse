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

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.metaverse.api.model.BaseInfo;

/**
 * User: RFellows Date: 11/3/14
 */
public class FieldInfo extends BaseInfo implements IFieldInfo {
  private String dataType;
  private Integer precision;
  private Integer length;
  private String stepName;

  public FieldInfo() {
  }

  public FieldInfo( ValueMetaInterface vmi ) {
    setName( vmi.getName() );
    setDescription( vmi.getComments() );
    setDataType( vmi.getTypeDesc() );
    setLength( vmi.getLength() );
    setPrecision( vmi.getPrecision() );
    setStepName( vmi.getOrigin() );
  }

  @Override
  public String getDataType() {
    return dataType;
  }

  @Override
  public Integer getPrecision() {
    return precision;
  }

  @Override
  public Integer getLength() {
    return length;
  }

  @Override
  public String getStepName() {
    return stepName;
  }

  public void setDataType( String dataType ) {
    this.dataType = dataType;
  }

  public void setPrecision( Integer precision ) {
    this.precision = precision;
  }

  public void setLength( Integer length ) {
    this.length = length;
  }

  public void setStepName( String stepName ) {
    this.stepName = stepName;
  }
}

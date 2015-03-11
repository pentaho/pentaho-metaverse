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

package com.pentaho.metaverse.api.model.kettle;

import com.pentaho.metaverse.api.model.BaseInfo;
import org.pentaho.di.core.row.ValueMetaInterface;

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

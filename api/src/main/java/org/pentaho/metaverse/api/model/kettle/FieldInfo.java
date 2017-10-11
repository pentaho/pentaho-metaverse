/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

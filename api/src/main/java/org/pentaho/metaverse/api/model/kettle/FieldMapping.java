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

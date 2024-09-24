/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2021 by Hitachi Vantara : http://www.pentaho.com
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


package org.pentaho.metaverse.api.model.catalog;

import java.util.Objects;

import static org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil.safeStringMatch;

public class FieldLevelRelationship {

  private LineageDataResource inputSourceResource;
  private String inputSourceResourceField;
  private LineageDataResource outputTargetResource;
  private String outputTargetResourceField;

  public FieldLevelRelationship() {
    inputSourceResourceField = null;
    inputSourceResource = null;
    outputTargetResourceField = null;
    outputTargetResource = null;
  }

  public FieldLevelRelationship( LineageDataResource inputSourceResource, LineageDataResource outputTargetResource,
                                 String inputSourceResourceField, String outputTargetResourceField ) {
    this.inputSourceResource = inputSourceResource;
    this.outputTargetResource = outputTargetResource;
    this.inputSourceResourceField = inputSourceResourceField;
    this.outputTargetResourceField = outputTargetResourceField;
  }

  public LineageDataResource getInputSourceResource() {
    return inputSourceResource;
  }

  public void setInputSourceResource( LineageDataResource inputSourceResource ) {
    this.inputSourceResource = inputSourceResource;
  }

  public String getInputSourceResourceField() {
    return inputSourceResourceField;
  }

  public void setInputSourceResourceField( String inputSourceResourceField ) {
    this.inputSourceResourceField = inputSourceResourceField;
  }

  public LineageDataResource getOutputTargetResource() {
    return outputTargetResource;
  }

  public void setOutputTargetResource( LineageDataResource outputTargetResource ) {
    this.outputTargetResource = outputTargetResource;
  }

  public String getOutputTargetResourceField() {
    return outputTargetResourceField;
  }

  public void setOutputTargetResourceField( String outputTargetResourceField ) {
    this.outputTargetResourceField = outputTargetResourceField;
  }

  @Override
  public String toString() {
    return inputSourceResource.getName() + ":" + inputSourceResourceField
            + " -> "
            + outputTargetResource.getName() + ":" + outputTargetResourceField;
  }

  @Override
  public boolean equals( Object o ) {
    if ( o instanceof FieldLevelRelationship ) {
      FieldLevelRelationship r2 = ( FieldLevelRelationship ) o;
      return this.inputSourceResource.shallowEquals( r2.getInputSourceResource() )
        && this.outputTargetResource.shallowEquals( r2.getOutputTargetResource() )
        && safeStringMatch( this.inputSourceResourceField, r2.getInputSourceResourceField() )
        && safeStringMatch( this.outputTargetResourceField, r2.getOutputTargetResourceField() );
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash( inputSourceResource, inputSourceResourceField, outputTargetResource, outputTargetResourceField );
  }
}

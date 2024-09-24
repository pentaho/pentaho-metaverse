/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

public class BaseResourceInfo extends BaseInfo implements IExternalResourceInfo {

  private String type;
  private Boolean isInput = false;

  private Map<Object, Object> attributes = new HashMap<Object, Object>();

  @Override
  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  @Override
  public boolean isInput() {
    return isInput;
  }

  @JsonIgnore
  @Override
  public boolean isOutput() {
    return !isInput;
  }

  public void setInput( boolean isInput ) {
    this.isInput = isInput;
  }

  @Override
  public Map<Object, Object> getAttributes() {
    return attributes;
  }

  public void putAttribute( Object key, Object value ) {
    attributes.put( key, value );
  }

  @Override
  public boolean equals( final Object obj ) {
    if ( obj == null ) {
      return false;
    }
    if ( obj == this ) {
      return true;
    }
    if ( obj.getClass() != getClass() ) {
      return false;
    }
    final BaseResourceInfo info = (BaseResourceInfo) obj;
    return new EqualsBuilder().append( getName(), info.getName() ).append( getDescription(), info.getDescription() )
      .append( getType(), info.getType() ).append( isInput(), info.isInput() )
      .append( getAttributes(), info.getAttributes() ).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder( 17, 37 ).append( getName() ).append( getDescription() ).append( getType() )
      .append( isInput() ).append( getAttributes() ).toHashCode();
  }
}

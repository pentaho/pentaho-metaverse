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

package org.pentaho.metaverse.api.model;


import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;

/**
 * Created by mburgess on 2/4/15.
 */
public class Operation implements IOperation {

  private String category;
  private ChangeType type;
  private String name;
  private String description;

  private Operation() {
  }

  public Operation( final String category, final ChangeType type, final String name, final String description ) {
    this();
    this.category = category;
    this.type = type;
    this.name = name;
    this.description = description;
  }

  // This partial constructor is for convenience and creates a new metadata operation
  public Operation( final String name, final String description ) {
    this( METADATA_CATEGORY, ChangeType.METADATA, name, description );
  }

  @Override
  public String getCategory() {
    return category;
  }

  @Override
  public void setCategory( String category ) {
    this.category = category;
  }

  @Override
  public ChangeType getType() {
    return type;
  }

  @Override
  public void setType( ChangeType type ) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  @Override
  public String toString() {
    return getName() + ": " + getDescription();
  }

  /**
   * Convenience method to quickly get a "rename" operation object
   * @return
   */
  public static Operation getRenameOperation() {
    return new Operation( METADATA_CATEGORY, ChangeType.METADATA, DictionaryConst.PROPERTY_MODIFIED, "name" );
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    Operation operation = (Operation) o;

    if ( category != null ? !category.equals( operation.category ) : operation.category != null ) {
      return false;
    }
    if ( description != null ? !description.equals( operation.description ) : operation.description != null ) {
      return false;
    }
    if ( name != null ? !name.equals( operation.name ) : operation.name != null ) {
      return false;
    }
    if ( type != operation.type ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = category != null ? category.hashCode() : 0;
    result = 31 * result + type.hashCode();
    result = 31 * result + ( name != null ? name.hashCode() : 0 );
    result = 31 * result + ( description != null ? description.hashCode() : 0 );
    return result;
  }
}

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

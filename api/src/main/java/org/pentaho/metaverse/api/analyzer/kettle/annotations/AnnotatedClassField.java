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

package org.pentaho.metaverse.api.analyzer.kettle.annotations;

import com.google.common.base.Objects;

import java.lang.annotation.Annotation;


/**
 * Container for a single annotated field.
 */
public class AnnotatedClassField<T extends Annotation> {

  final T annotation;
  final String name;
  private final String val;

  AnnotatedClassField( T annotation, String fieldName, String fieldVal ) {
    this.annotation = annotation;
    this.name = fieldName;
    this.val = fieldVal;
  }

  String name() {
    return name;
  }

  String val() {
    return val;
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    AnnotatedClassField<?> that = (AnnotatedClassField<?>) o;
    return Objects.equal( annotation, that.annotation )
      && Objects.equal( name, that.name )
      && Objects.equal( val, that.val );
  }

  @Override public int hashCode() {
    return Objects.hashCode( annotation, name, val );
  }
}

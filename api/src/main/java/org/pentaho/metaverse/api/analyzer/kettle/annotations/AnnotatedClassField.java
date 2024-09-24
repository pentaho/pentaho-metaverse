/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

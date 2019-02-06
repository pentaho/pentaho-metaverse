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

import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.trans.step.BaseStepMeta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.stream;

/**
 * Convenience class for navigating the @Metaverse.Node, @Metaverse.Property,
 * and @Metaverse.NodeLink annotations in a class.
 */
class AnnotatedClassFields {

  private final BaseStepMeta meta;

  AnnotatedClassFields( BaseStepMeta meta ) {
    this.meta = meta;
  }

  Stream<AnnotatedClassField<Metaverse.Node>> nodes() {
    return recurseObjectTree( meta, Metaverse.Node.class );
  }

  Optional<AnnotatedClassField<Metaverse.Node>> node( String nodeName ) {
    return nodes()
      .filter( n -> n.annotation.name().equals( nodeName ) )
      .findFirst();

  }

  Stream<AnnotatedClassField<Metaverse.NodeLink>> links() {
    return recurseObjectTree( meta, Metaverse.NodeLink.class );
  }

  Stream<AnnotatedClassField<Metaverse.Property>> props() {
    return recurseObjectTree( meta, Metaverse.Property.class );
  }

  private <T extends Annotation> Stream<AnnotatedClassField<T>> recurseObjectTree( Object object, Class<T> anno ) {
    return stream( object.getClass().getFields() )
      .flatMap( field -> getAnnotatedFieldStream( object, anno, field ) );
  }

  private <T extends Annotation> Stream<? extends AnnotatedClassField<T>> getAnnotatedFieldStream(
    Object object, Class<T> annotation, Field field ) {
    if ( field.isAnnotationPresent( InjectionDeep.class ) ) {
      try {
        return recurseObjectTree( field.get( object ), annotation );
      } catch ( IllegalAccessException e ) {
        throw new IllegalStateException( e );
      }
    } else if ( field.isAnnotationPresent( annotation ) ) {
      return Stream.of( new AnnotatedClassField<>(
        field.getAnnotation( annotation ),
        getName( object, field, annotation ),
        getValue( object, field ) ) );
    } else {
      return Stream.empty();
    }
  }

  private boolean isProperty( Field field ) {
    return field.isAnnotationPresent( Metaverse.Property.class );
  }

  private boolean isNode( Field field ) {
    return field.isAnnotationPresent( Metaverse.Node.class );
  }

  private String getValue( Object object, Field field ) {
    try {
      String value = field.get( object ).toString();
      return meta.getParentStepMeta().getParentTransMeta().environmentSubstitute( value );
    } catch ( IllegalAccessException e ) {
      throw new IllegalStateException( e );
    }
  }

  private String getName( Object object, Field field,
                          Class<? extends Annotation> annotation ) {
    if ( isNode( field ) && annotation == Metaverse.Node.class ) {
      // nodes will default to being named based on the field's value,
      // if it has been populated.
      return getNodeName( field, object );
    } else {
      return deriveName( object, field );
    }
  }

  private String getNodeName( Field nodeField, Object object ) {
    String metaFieldValue = getValue( object, nodeField );
    if ( isNullOrEmpty( metaFieldValue ) ) {
      metaFieldValue = deriveName( object, nodeField );
    }
    return metaFieldValue;
  }

  /**
   * Retrieves a value to be used for an element's name.  For Metaverse.Property
   * elements, will use the value of the name attribute.
   * Otherwise will fall back to the first non empty value of
   *   - field val
   *   - field name
   */
  private String deriveName( Object object, Field field ) {
    String annotatedName = null;
    if ( isProperty( field ) ) {
      annotatedName = field.getAnnotation( Metaverse.Property.class ).name();
    }
    if ( isNullOrEmpty( annotatedName ) ) {
      annotatedName = getValue( object, field );
    }
    if ( isNullOrEmpty( annotatedName ) ) {
      annotatedName = field.getName().toLowerCase();
    }
    return annotatedName;
  }


}

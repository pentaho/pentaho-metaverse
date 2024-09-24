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
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse.InternalStepMeta;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.stream;

/**
 * Convenience class for navigating the @Metaverse.Node, @Metaverse.Property,
 * and @Metaverse.NodeLink annotations in a class.
 */
public class AnnotatedClassFields {

  private final Object meta;
  private final VariableSpace variableSpace;

  public AnnotatedClassFields( BaseStepMeta meta ) {
    this.meta = meta;
    variableSpace = meta.getParentStepMeta().getParentTransMeta();
  }

  public AnnotatedClassFields( Object meta, VariableSpace variableSpace ) {
    this.meta = meta;
    this.variableSpace = variableSpace;
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
    return Stream.concat( stream( object.getClass().getFields() ), stream( object.getClass().getMethods() ) )
      .flatMap( field -> getAnnotatedFieldStream( object, anno, field ) );
  }

  public boolean hasMetaverseAnnotations() {
    return props().count() > 0 || nodes().count() > 0 || links().count() > 0;
  }

  private <T extends Annotation> Stream<? extends AnnotatedClassField<T>> getAnnotatedFieldStream(
    Object object, Class<T> annotation, AccessibleObject accessibleObject ) {
    if ( accessibleObject.isAnnotationPresent( InjectionDeep.class )
      || accessibleObject.isAnnotationPresent( InternalStepMeta.class ) ) {
      try {
        return recurseObjectTree( accessibleValue( object, accessibleObject ), annotation );
      } catch ( IllegalAccessException | InvocationTargetException e ) {
        throw new IllegalStateException( e );
      }
    } else if ( accessibleObject.isAnnotationPresent( annotation ) ) {
      return Stream.of( new AnnotatedClassField<>(
        accessibleObject.getAnnotation( annotation ),
        getName( object, accessibleObject, annotation ),
        getValue( object, accessibleObject ) ) );
    } else {
      return Stream.empty();
    }
  }

  private boolean isProperty( AccessibleObject accessibleObject ) {
    return accessibleObject.isAnnotationPresent( Metaverse.Property.class );
  }

  private boolean isNode( AccessibleObject accessibleObject ) {
    return accessibleObject.isAnnotationPresent( Metaverse.Node.class );
  }

  private String getValue( Object object, AccessibleObject field ) {
    try {
      Object accessibleValue = accessibleValue( object, field );
      String value = accessibleValue.toString();
      return variableSpace.environmentSubstitute( value );
    } catch ( IllegalAccessException | InvocationTargetException e ) {
      throw new IllegalStateException( e );
    }
  }

  private Object accessibleValue( Object object, AccessibleObject accessibleObject )
    throws IllegalAccessException, InvocationTargetException {
    return accessibleObject instanceof Field
      ? ( (Field) accessibleObject ).get( object )
      : ( (Method) accessibleObject ).invoke( object );
  }

  private String getName( Object object, AccessibleObject accessibleObject,
                          Class<? extends Annotation> annotation ) {

    if ( isNode( accessibleObject ) && annotation == Metaverse.Node.class ) {
      if ( ( (Metaverse.Node) accessibleObject.getAnnotation( annotation )).nameFromValue().equals( Metaverse.TRUE ) ) {
        // nodes will default to being named based on the field's value,
        // if it has been populated.
        return getNodeName( accessibleObject, object );
      } else {
        return ( (Metaverse.Node) accessibleObject.getAnnotation( annotation ) ).name();
      }
    } else {
      return deriveName( object, accessibleObject );
    }
  }

  private String getNodeName( AccessibleObject accessibleObject, Object object ) {
    String metaFieldValue = getValue( object, accessibleObject );
    if ( isNullOrEmpty( metaFieldValue ) ) {
      metaFieldValue = deriveName( object, accessibleObject );
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
  private String deriveName( Object object, AccessibleObject field ) {
    String annotatedName = null;
    if ( isProperty( field ) ) {
      annotatedName = field.getAnnotation( Metaverse.Property.class ).name();
    }
    if ( isNullOrEmpty( annotatedName ) ) {
      annotatedName = getValue( object, field );
    }
    if ( isNullOrEmpty( annotatedName ) ) {
      String name = field instanceof Field
        ? ( (Field) field ).getName()
        : ( (Method) field ).getName();
      annotatedName = name.toLowerCase();
    }
    return annotatedName;
  }


}

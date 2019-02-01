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

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.pentaho.dictionary.DictionaryConst.CATEGORY_FIELD;
import static org.pentaho.dictionary.DictionaryConst.LINK_CONTAINS;
import static org.pentaho.dictionary.DictionaryConst.LINK_DEPENDENCYOF;
import static org.pentaho.dictionary.DictionaryConst.NODE_TYPE_TRANS_FIELD;

/**
 * Annotations for defining metaverse nodes/links/properties on a meta class.
 * See README.md for details.
 */
public @interface Metaverse {

  @Target ( { FIELD, METHOD } )
  @Retention ( RUNTIME ) @interface Node {
    String name();

    String type();

    String link() default LINK_DEPENDENCYOF; // link to step node
  }

  @Target ( { FIELD, METHOD } )
  @Retention ( RUNTIME ) @interface NodeLink {
    String nodeName() default "";

    String parentNodeName();

    String parentNodelink() default LINK_CONTAINS;

    String linkDirection() default "IN";  // Direction.OUT / IN
  }

  @Target ( { FIELD, METHOD } )
  @Retention ( RUNTIME ) @interface Property {
    String name() default "";

    String type() default NODE_TYPE_TRANS_FIELD;

    String category() default CATEGORY_FIELD;

    String parentNodeName() default "";
  }


  @Target ( TYPE )
  @Repeatable ( CategoryMaps.class )
  @Retention ( RUNTIME ) @interface CategoryMap {
    String entity();

    String category();
  }

  @Target ( TYPE )
  @Retention ( RUNTIME ) @interface CategoryMaps {
    CategoryMap[] value();
  }

  @Target ( TYPE )
  @Repeatable ( EntityLinks.class )
  @Retention ( RUNTIME ) @interface EntityLink {
    String entity();

    String parentEntity() default "";

    String link();
  }

  @Target ( TYPE )
  @Retention ( RUNTIME ) @interface EntityLinks {
    EntityLink[] value();
  }


}

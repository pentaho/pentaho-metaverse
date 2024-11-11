/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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

  String SUBTRANS_INPUT = "SUBTRANS_INPUT";
  String SUBTRANS_OUTPUT = "SUBTRANS_OUTPUT";
  String SUBTRANS_NONE = "SUBTRANS_NONE";
  String TRUE = "TRUE";
  String FALSE = "FALSE";

  @Target ( { FIELD, METHOD } )
  @Retention ( RUNTIME ) @interface Node {
    String name();

    String type();

    String link() default LINK_DEPENDENCYOF; // link to step node

    String linkDirection() default "OUT";

    String nameFromValue() default TRUE; // name in field value will be the name of the metaverse node by default

    String subTransLink() default SUBTRANS_NONE; // indicates if this field should be linked to subtrans INPUT or OUTPUT
  }

  @Target ( { FIELD, METHOD } )
  @Repeatable( NodeLinks.class )
  @Retention ( RUNTIME ) @interface NodeLink {
    String nodeName() default "";

    String parentNodeName();

    String parentNodelink() default LINK_CONTAINS;

    String linkDirection() default "IN";  // Direction.OUT / IN
  }

  @Target ( { FIELD, METHOD } )
  @Retention ( RUNTIME ) @interface NodeLinks {
    NodeLink[] value();
  }

  @Target ( { FIELD, METHOD } )
  @Retention ( RUNTIME ) @interface Property {
    String name() default "";

    String type() default NODE_TYPE_TRANS_FIELD;

    String category() default CATEGORY_FIELD;

    String parentNodeName() default "";
  }

  @Target ( { METHOD } )
  @Retention ( RUNTIME ) @interface InternalStepMeta {
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

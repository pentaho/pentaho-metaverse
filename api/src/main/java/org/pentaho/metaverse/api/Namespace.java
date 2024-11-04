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


package org.pentaho.metaverse.api;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.pentaho.dictionary.DictionaryConst;

/**
 * This is the default implementation for namespace objects and includes methods for working with namespaces
 */
public class Namespace implements INamespace {

  // Single re-usable ObjectMapper for JSON-to-Java conversions
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private String namespace;

  public Namespace( String namespace ) {
    this.namespace = namespace;
  }

  @Override
  public String getNamespaceId() {
    return namespace;
  }

  @Override
  public INamespace getParentNamespace() {
    if ( namespace != null ) {
      try {
        JsonNode jsonObject = objectMapper.readTree( namespace );
        JsonNode namespaceNode = jsonObject.get( DictionaryConst.PROPERTY_NAMESPACE );
        if ( namespaceNode == null ) {
          return null;
        }
        String parent;

        if ( namespaceNode.isTextual() ) {
          parent = namespaceNode.asText();
        } else {
          parent = namespaceNode.toString();
        }

        return new Namespace( parent );
      } catch ( Exception e ) {
        return null;
      }
    }
    return null;
  }

  @Override
  public INamespace getSiblingNamespace( String name, String type ) {
    if ( namespace != null ) {
      try {
        JsonNode jsonObject = objectMapper.readTree( namespace );

        if ( jsonObject.isObject() ) {
          ObjectNode object = (ObjectNode) jsonObject;
          object.put( DictionaryConst.PROPERTY_NAME, name );
          object.put( DictionaryConst.PROPERTY_TYPE, type );
        }

        return new Namespace( objectMapper.writeValueAsString( jsonObject ) );
      } catch ( Exception e ) {
        return null;
      }
    }
    return null;
  }
}

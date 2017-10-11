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

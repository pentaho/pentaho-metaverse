/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.api;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pentaho.dictionary.DictionaryConst;

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

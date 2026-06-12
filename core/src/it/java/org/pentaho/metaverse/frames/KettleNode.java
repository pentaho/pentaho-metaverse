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


package org.pentaho.metaverse.frames;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.pentaho.dictionary.DictionaryConst;

import java.util.List;

/**
 * User: RFellows Date: 9/4/14
 */
public class KettleNode extends Concept {
  public KettleNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public String getPath() {
    return getStringValue( DictionaryConst.PROPERTY_PATH );
  }

  public String getVersion() {
    return getStringValue( DictionaryConst.PROPERTY_ARTIFACT_VERSION );
  }

  public String getExtendedDescription() {
    return getStringValue( "extendedDescription" );
  }

  public String getStatus() {
    return getStringValue( DictionaryConst.PROPERTY_STATUS );
  }

  public String getLastModified() {
    return getStringValue( DictionaryConst.PROPERTY_LAST_MODIFIED );
  }

  public String getLastModifiedBy() {
    return getStringValue( DictionaryConst.PROPERTY_LAST_MODIFIED_BY );
  }

  public String getCreated() {
    return getStringValue( DictionaryConst.PROPERTY_CREATED );
  }

  public String getCreatedBy() {
    return getStringValue( DictionaryConst.PROPERTY_CREATED_BY );
  }

  public LocatorNode getLocator() {
    List<Vertex> result = graph.traversal().V( vertex.id() ).in( "contains" ).has( "type", "Locator" ).toList();
    return result.isEmpty() ? null : new LocatorNode( result.get( 0 ), graph );
  }

  public String getParameter( String paramKey ) {
    Object value = getProperty( paramKey );
    return value == null ? null : value.toString();
  }
}

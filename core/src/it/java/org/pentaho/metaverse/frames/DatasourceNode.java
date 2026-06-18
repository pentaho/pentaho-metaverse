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

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

/**
 * User: RFellows Date: 9/4/14
 */
public class DatasourceNode extends Concept {
  public DatasourceNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public String getPort() {
    return getStringValue( "port" );
  }

  public String getHost() {
    return getStringValue( "host" );
  }

  public String getHostName() {
    return getStringValue( "hostName" );
  }

  public String getUserName() {
    return getStringValue( "userName" );
  }

  public String getPassword() {
    return getStringValue( "password" );
  }

  public Integer getAccessType() {
    return getIntegerValue( "accessType" );
  }

  public String getAccessTypeDesc() {
    return getStringValue( "accessTypeDesc" );
  }

  public String getDatabaseName() {
    return getStringValue( "databaseName" );
  }

  public String getSchema() {
    return getStringValue( "schema" );
  }

  public List<TransformationStepNode> getTransformationStepNodes() {
    return wrapAs( vertex.vertices( Direction.OUT, "dependencyof" ), v -> new TransformationStepNode( v, graph ) );
  }
}

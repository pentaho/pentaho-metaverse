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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;

/**
 * User: RFellows Date: 9/4/14
 */
public interface DatasourceNode extends Concept {
  @Property( "port" )
  public String getPort();
  @Property( "host" )
  public String getHost();
  @Property( "hostName" )
  public String getHostName();
  @Property( "userName" )
  public String getUserName();
  @Property( "password" )
  public String getPassword();
  @Property( "accessType" )
  public Integer getAccessType();
  @Property( "accessTypeDesc" )
  public String getAccessTypeDesc();
  @Property( "databaseName" )
  public String getDatabaseName();
  @Property( "schema" )
  public String getSchema();

  @Adjacency( label = "dependencyof", direction = Direction.OUT )
  public Iterable<TransformationStepNode> getTransformationStepNodes();

}

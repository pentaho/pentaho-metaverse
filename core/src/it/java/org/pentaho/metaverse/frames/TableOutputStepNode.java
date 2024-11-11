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
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import org.pentaho.metaverse.analyzer.kettle.step.tableoutput.TableOutputStepAnalyzer;

/**
 * User: RFellows Date: 9/4/14
 */
public interface TableOutputStepNode extends TransformationStepNode {
  @Adjacency( label = "dependencyof", direction = Direction.IN )
  public Iterable<DatasourceNode> getDatasources();

  @GremlinGroovy( "it.in('dependencyof').has( 'name', T.eq, name )" )
  public DatasourceNode getDatasource( @GremlinParam( "name") String name );

  @Adjacency( label = "writesto", direction = Direction.OUT )
  public DatabaseTableNode getDatabaseTable();

  @Property( "schema" )
  public String getSchema();

  @Property( TableOutputStepAnalyzer.TRUNCATE_TABLE )
  public Boolean isTruncateTable();
}

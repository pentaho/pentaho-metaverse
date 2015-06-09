package org.pentaho.metaverse.frames;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;


public interface MongoDbDatasourceNode extends Concept {
  @Property( "port" )
  public String getPort();
  @Property( "hostnames" )
  public String getHostnames();
  @Property( "userName" )
  public String getUserName();
  @Property( "password" )
  public String getPassword();
  @Property( "databaseName" )
  public String getDatabaseName();

  @Adjacency( label = "dependencyof", direction = Direction.OUT )
  public Iterable<TransformationStepNode> getTransformationStepNodes();
}

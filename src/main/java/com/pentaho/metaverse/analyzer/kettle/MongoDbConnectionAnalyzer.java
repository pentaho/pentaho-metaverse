package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.impl.MetaverseLogicalIdGenerator;
import org.pentaho.di.trans.steps.mongodb.MongoDbMeta;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.ILogicalIdGenerator;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.ArrayList;
import java.util.List;

/**
 * User: RFellows Date: 3/6/15
 */
public class MongoDbConnectionAnalyzer extends BaseKettleMetaverseComponent implements
    IConnectionAnalyzer<MongoDbMeta, MongoDbMeta> {

  public static final String HOST_NAMES = "hostnames";
  public static final String DATABASE_NAME = "databaseName";

  public static final ILogicalIdGenerator ID_GENERATOR = new MetaverseLogicalIdGenerator(
      HOST_NAMES,
      DATABASE_NAME,
      DictionaryConst.PROPERTY_PORT,
      DictionaryConst.PROPERTY_USER_NAME
  );

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, MongoDbMeta mongoDbMeta )
    throws MetaverseAnalyzerException {

    IMetaverseNode datasourceNode = createNodeFromDescriptor( descriptor );

    String database = mongoDbMeta.getDbName();
    String port = mongoDbMeta.getPort();
    String hostNames = mongoDbMeta.getHostnames();
    String user = mongoDbMeta.getAuthenticationUser();

    datasourceNode.setProperty( HOST_NAMES, hostNames );
    datasourceNode.setProperty( DATABASE_NAME, database );
    datasourceNode.setProperty( DictionaryConst.PROPERTY_USER_NAME, user );
    datasourceNode.setProperty( DictionaryConst.PROPERTY_PORT, port );
    datasourceNode.setLogicalIdGenerator( getLogicalIdGenerator() );

    return datasourceNode;
  }

  @Override
  public List<MongoDbMeta> getUsedConnections( MongoDbMeta meta ) {
    List<MongoDbMeta> metas = new ArrayList<MongoDbMeta>();
    metas.add( meta );
    return metas;
  }

  @Override
  public IComponentDescriptor buildComponentDescriptor( IComponentDescriptor parentDescriptor,
                                                        MongoDbMeta connection ) {

    IComponentDescriptor dbDescriptor = new MetaverseComponentDescriptor(
        connection.getDbName(),
        DictionaryConst.NODE_TYPE_MONGODB_CONNECTION,
        parentDescriptor.getNamespace(),
        parentDescriptor.getContext() );

    return dbDescriptor;
  }

  @Override
  protected ILogicalIdGenerator getLogicalIdGenerator() {
    return ID_GENERATOR;
  }
}

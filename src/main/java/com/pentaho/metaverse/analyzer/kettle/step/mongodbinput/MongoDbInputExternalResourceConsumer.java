package com.pentaho.metaverse.analyzer.kettle.step.mongodbinput;

import com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.step.BaseStepExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumer;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.impl.model.MongoDbResourceInfo;
import org.pentaho.di.trans.steps.mongodbinput.MongoDbInput;
import org.pentaho.di.trans.steps.mongodbinput.MongoDbInputMeta;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@ExternalResourceConsumer(
  id = "MongoDbInputExternalResourceConsumer",
  name = "MongoDbInputExternalResourceConsumer"
)
public class MongoDbInputExternalResourceConsumer
  extends BaseStepExternalResourceConsumer<MongoDbInput, MongoDbInputMeta> {

  @Override
  public Class<MongoDbInputMeta> getMetaClass() {
    return MongoDbInputMeta.class;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( MongoDbInputMeta meta ) {
    Set<IExternalResourceInfo> resources = new HashSet<IExternalResourceInfo>();
    MongoDbResourceInfo mongoDbResourceInfo = new MongoDbResourceInfo( meta );
    mongoDbResourceInfo.setInput( true );
    resources.add( mongoDbResourceInfo );
    return resources;
  }
}

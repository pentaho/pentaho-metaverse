package com.pentaho.metaverse.tableinput;

import com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.step.BaseStepExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumer;
import com.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.steps.tableinput.TableInput;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@ExternalResourceConsumer(
  id = "TableInputExternalResourceConsumer",
  name = "TableInputExternalResourceConsumer"
)public class TableInputExternalResourceConsumer extends BaseStepExternalResourceConsumer<TableInput, TableInputMeta> {
  @Override
  public Class<TableInputMeta> getMetaClass() {
    return TableInputMeta.class;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( TableInputMeta meta ) {

    Set<IExternalResourceInfo> resources = new HashSet<IExternalResourceInfo>();
    DatabaseMeta dbMeta = meta.getDatabaseMeta();
    if ( dbMeta != null ) {
      resources.add( ExternalResourceInfoFactory.createDatabaseResource( dbMeta ) );
    }
    return resources;
  }
}

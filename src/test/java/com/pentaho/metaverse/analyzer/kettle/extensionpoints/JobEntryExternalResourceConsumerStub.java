package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.job.entry.JobEntryBase;

import java.util.Collection;

/**
 * Helper class to provide an implementation of IJobEntryExternalResourceConsumer for testing
 */
public class JobEntryExternalResourceConsumerStub implements IJobEntryExternalResourceConsumer {

  @Override
  public boolean isDataDriven( Object meta ) {
    return false;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( Object meta ) {
    return null;
  }

  @Override
  public Class<?> getMetaClass() {
    return JobEntryBase.class;
  }
}

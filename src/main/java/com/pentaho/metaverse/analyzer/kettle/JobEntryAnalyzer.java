package com.pentaho.metaverse.analyzer.kettle;

import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.platform.api.metaverse.IAnalyzer;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;

/**
 * Created by gmoran on 7/16/14.
 */
public class JobEntryAnalyzer extends AbstractAnalyzer<JobEntryCopy> {

  @Override
  public IMetaverseNode analyze( JobEntryCopy object ) throws MetaverseAnalyzerException {
    return null;
  }

}

package com.pentaho.metaverse.analyzer.kettle.jobentry;

import com.pentaho.dictionary.DictionaryConst;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by gmoran on 7/30/14.
 */
public class TransJobEntryAnalyzer extends BaseJobEntryAnalyzer<JobEntryTrans> {

  @Override
  public IMetaverseNode analyze( IMetaverseComponentDescriptor descriptor, JobEntryTrans entry )
    throws MetaverseAnalyzerException {

    IMetaverseNode node = super.analyze( descriptor, entry );

    String entryFilename = entry.getFilename();
    if ( entryFilename != null ) {
      String filename = entry.getParentJob().getJobMeta().environmentSubstitute( entryFilename );

      IMetaverseNode transformationNode = createNodeFromDescriptor(
          getChildComponentDescriptor(
              getSiblingNamespace( descriptor, filename, DictionaryConst.NODE_TYPE_TRANS ),
              filename,
              DictionaryConst.NODE_TYPE_TRANS ) );

      metaverseBuilder.addLink( node, DictionaryConst.LINK_CONTAINS, transformationNode );
    }

    return node;
  }

  @Override public Set<Class<? extends JobEntryInterface>> getSupportedEntries() {
    Set<Class<? extends JobEntryInterface>> supportedEntries = new HashSet<Class<? extends JobEntryInterface>>();
    supportedEntries.add( JobEntryTrans.class );
    return supportedEntries;
  }
}

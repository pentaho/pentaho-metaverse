package com.pentaho.metaverse.analyzer.kettle.jobentry;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.net.URI;
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
      URI uri = URI.create( filename );

      IMetaverseComponentDescriptor ds = new MetaverseComponentDescriptor(
          filename,
          DictionaryConst.NODE_TYPE_TRANS,
          descriptor.getNamespace().getParentNamespace().getParentNamespace()
              .getChildNamespace( uri.getPath(), DictionaryConst.NODE_TYPE_TRANS ) );

      IMetaverseNode transformationNode = createNodeFromDescriptor( ds );

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

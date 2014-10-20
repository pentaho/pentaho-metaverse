package com.pentaho.metaverse.analyzer.kettle.jobentry;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.KettleAnalyzerUtil;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by gmoran on 7/30/14.
 */
public class TransJobEntryAnalyzer extends BaseJobEntryAnalyzer<JobEntryTrans> {

  private Logger log = LoggerFactory.getLogger( TransJobEntryAnalyzer.class );

  @Override
  public IMetaverseNode analyze( IMetaverseComponentDescriptor descriptor, JobEntryTrans entry )
    throws MetaverseAnalyzerException {

    IMetaverseNode node = super.analyze( descriptor, entry );
    String entryFilename = entry.getFilename();
    String name = entry.getName();

    if ( entryFilename != null ) {
      String filename = entry.getParentJob().getJobMeta().environmentSubstitute( entryFilename );

      String normalized = null;
      try {
        normalized = KettleAnalyzerUtil.normalizeFilePath( filename );
        FileInputStream fis = new FileInputStream( normalized );
        TransMeta tm = new TransMeta( fis, null, true, null, null );
        name = tm.getName();
      } catch ( Exception e ) {
        log.error( e.getMessage(), e );
      }

      IMetaverseComponentDescriptor ds = new MetaverseComponentDescriptor(
          name,
          DictionaryConst.NODE_TYPE_TRANS,
          descriptor.getNamespace().getParentNamespace() );

      IMetaverseNode transformationNode = createNodeFromDescriptor( ds );
      transformationNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, ds.getNamespaceId() );
      transformationNode.setProperty( DictionaryConst.PROPERTY_PATH, normalized );
      transformationNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

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

/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */
package com.pentaho.metaverse.analyzer.kettle.jobentry.transjob;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.KettleAnalyzerUtil;
import com.pentaho.metaverse.analyzer.kettle.jobentry.BaseJobEntryAnalyzer;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.TransMeta;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides an analyzer for the "Execute Transformation" job entry
 */
public class TransJobEntryAnalyzer extends BaseJobEntryAnalyzer<JobEntryTrans> {

  private Logger log = LoggerFactory.getLogger( TransJobEntryAnalyzer.class );

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, JobEntryTrans entry )
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

      IComponentDescriptor ds = new MetaverseComponentDescriptor(
          name,
          DictionaryConst.NODE_TYPE_TRANS,
          descriptor.getNamespace().getParentNamespace() );

      IMetaverseNode transformationNode = createNodeFromDescriptor( ds );
      transformationNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, ds.getNamespaceId() );
      transformationNode.setProperty( DictionaryConst.PROPERTY_PATH, normalized );
      transformationNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

      metaverseBuilder.addLink( node, DictionaryConst.LINK_EXECUTES, transformationNode );
    }

    return node;
  }

  @Override public Set<Class<? extends JobEntryInterface>> getSupportedEntries() {
    Set<Class<? extends JobEntryInterface>> supportedEntries = new HashSet<Class<? extends JobEntryInterface>>();
    supportedEntries.add( JobEntryTrans.class );
    return supportedEntries;
  }
}

/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.analyzer.kettle.jobentry;

import com.pentaho.metaverse.analyzer.kettle.BaseKettleMetaverseComponent;
import com.pentaho.metaverse.analyzer.kettle.IDatabaseConnectionAnalyzer;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

/**
 * The JobEntryAnalyzer provides JobEntryCopy metadata to the metaverse.
 * <p/>
 * Created by gmoran on 7/16/14.
 */
public abstract class BaseJobEntryAnalyzer<T extends JobEntryInterface> extends BaseKettleMetaverseComponent
    implements IJobEntryAnalyzer<T> {

  /**
   * A reference to the root node created by the analyzer (usually corresponds to the step under analysis)
   */
  protected IMetaverseNode rootNode = null;

  /**
   * A reference to the database connection analyzer
   */
  protected IDatabaseConnectionAnalyzer dbConnectionAnalyzer = null;

  /**
   * A namespace to hold the fields derived in the step
   */
  private INamespace stepNamespace = null;

  /**
   * Analyzes job entries
   *
   * @param entry
   * @return
   * @throws MetaverseAnalyzerException
   */
  @Override
  public IMetaverseNode analyze( IMetaverseComponentDescriptor descriptor, T entry ) throws MetaverseAnalyzerException {

    validateState( descriptor, entry );

    // Add yourself
    IMetaverseNode node = createNodeFromDescriptor( descriptor );
    metaverseBuilder.addNode( node );

    return node;

  }

  protected void validateState( IMetaverseComponentDescriptor descriptor, T entry ) throws MetaverseAnalyzerException {

    if ( entry == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.JobEntryInterface.IsNull" ) );
    }

    Job parentJob = entry.getParentJob();

    if ( parentJob == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.ParentJob.IsNull" ) );
    }

    JobMeta parentJobMeta = parentJob.getJobMeta();
    if ( parentJobMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.ParentJobMeta.IsNull" ) );
    }

    if ( metaverseBuilder == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseBuilder.IsNull" ) );
    }

    if ( metaverseObjectFactory == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseObjectFactory.IsNull" ) );
    }
  }
}

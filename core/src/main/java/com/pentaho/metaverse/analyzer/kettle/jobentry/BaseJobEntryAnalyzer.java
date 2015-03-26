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

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.BaseKettleMetaverseComponent;
import com.pentaho.metaverse.api.IConnectionAnalyzer;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The JobEntryAnalyzer provides JobEntryCopy metadata to the metaverse.
 * <p/>
 * Created by gmoran on 7/16/14.
 */
public abstract class BaseJobEntryAnalyzer<T extends JobEntryInterface>
    extends BaseKettleMetaverseComponent implements IJobEntryAnalyzer<IMetaverseNode, T> {

  private static final Logger LOGGER = LoggerFactory.getLogger( BaseJobEntryAnalyzer.class );

  /**
   * A reference to the JobEntryInterface under analysis
   */
  protected T jobEntryInterface = null;

  /**
   * A reference to the entry's parent Job
   */
  protected Job parentJob = null;

  /**
   * A reference to the entry's parent JobMeta
   */
  JobMeta parentJobMeta = null;

  /**
   * A reference to the root node created by the analyzer (usually corresponds to the job entry under analysis)
   */
  protected IMetaverseNode rootNode = null;

  /**
   * A reference to the connection analyzer
   */
  protected IConnectionAnalyzer<Object, T> connectionAnalyzer = null;

  /**
   * Analyzes job entries
   *
   * @param entry
   * @return
   * @throws MetaverseAnalyzerException
   */
  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, T entry ) throws MetaverseAnalyzerException {

    validateState( descriptor, entry );

    // Add yourself
    rootNode = createNodeFromDescriptor( descriptor );
    String stepType = null;
    try {
      stepType = PluginRegistry.getInstance().findPluginWithId(
          JobEntryPluginType.class, entry.getPluginId() ).getName();
    } catch ( Throwable t ) {
      stepType = entry.getClass().getSimpleName();
    }
    rootNode.setProperty( "jobEntryType", stepType );
    rootNode.setProperty( "copies", entry.getParentJob().getJobMeta().getJobCopies().size() );
    metaverseBuilder.addNode( rootNode );

    return rootNode;
  }

  /**
   * Adds any used database connections to the metaverse using the appropriate analyzer
   *
   * @throws MetaverseAnalyzerException
   */
  protected void addConnectionNodes( IComponentDescriptor descriptor )
    throws MetaverseAnalyzerException {

    if ( jobEntryInterface == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.JobEntryInterface.IsNull" ) );
    }

    if ( connectionAnalyzer != null ) {
      List<? extends Object> connections = connectionAnalyzer.getUsedConnections( jobEntryInterface );
      for ( Object connection : connections ) {
        String connName = null;
        // see if the connection object has a getName method
        try {
          Method getNameMethod = connection.getClass().getMethod( "getName", null );
          connName = (String) getNameMethod.invoke( connection, null );
        } catch ( Exception e ) {
          // doesn't have a getName method, will try to get it from the descriptor later
        }
        try {
          IComponentDescriptor connDescriptor = connectionAnalyzer.buildComponentDescriptor( descriptor, connection );
          connName = connName == null ? descriptor.getName() : connName;
          IMetaverseNode connNode = connectionAnalyzer.analyze( connDescriptor, connection );
          metaverseBuilder.addLink( connNode, DictionaryConst.LINK_DEPENDENCYOF, rootNode );
        } catch ( Throwable t ) {
          // Don't throw the exception if a DB connection couldn't be analyzed, just log it and move on
          LOGGER.warn( Messages.getString( "WARNING.AnalyzingDatabaseConnection", connName ), t );
        }
      }
    }

  }

  protected void validateState( IComponentDescriptor descriptor, T entry ) throws MetaverseAnalyzerException {

    if ( entry == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.JobEntryInterface.IsNull" ) );
    }
    jobEntryInterface = entry;

    parentJob = entry.getParentJob();

    if ( parentJob == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.ParentJob.IsNull" ) );
    }

    parentJobMeta = parentJob.getJobMeta();
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

  public IConnectionAnalyzer<Object, T> getConnectionAnalyzer() {
    return connectionAnalyzer;
  }

  public void setConnectionAnalyzer( IConnectionAnalyzer<Object, T> connectionAnalyzer ) {
    this.connectionAnalyzer = connectionAnalyzer;
  }

}

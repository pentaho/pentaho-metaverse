/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.api.analyzer.kettle.jobentry;

import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IConnectionAnalyzer;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.analyzer.kettle.BaseKettleMetaverseComponent;
import org.pentaho.metaverse.api.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The JobEntryAnalyzer provides JobEntryCopy metadata to the metaverse.
 * <p/>
 * Created by gmoran on 7/16/14.
 */
public abstract class JobEntryAnalyzer<T extends JobEntryInterface> extends BaseKettleMetaverseComponent implements
  IJobEntryAnalyzer<IMetaverseNode, T> {

  private static final Logger LOGGER = LoggerFactory.getLogger( JobEntryAnalyzer.class );

  protected String[] prevJobNames = null;

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
   * A descriptor for creating this node
   */
  protected IComponentDescriptor descriptor;

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
    this.descriptor = descriptor;

    // Add yourself
    rootNode = createNodeFromDescriptor( descriptor );
    String stepType = null;
    try {
      stepType =
        PluginRegistry.getInstance().findPluginWithId( JobEntryPluginType.class, entry.getPluginId() ).getName();
    } catch ( Throwable t ) {
      stepType = entry.getClass().getSimpleName();
    }
    rootNode.setProperty( "pluginId", entry.getPluginId() );
    rootNode.setProperty( "jobEntryType", stepType );
    rootNode.setProperty( "copies", entry.getParentJob().getJobMeta().getJobCopies().size() );
    metaverseBuilder.addNode( rootNode );

    customAnalyze( entry, rootNode );

    return rootNode;
  }

  protected abstract void customAnalyze( T entry, IMetaverseNode rootNode ) throws MetaverseAnalyzerException;

  /**
   * Adds any used database connections to the metaverse using the appropriate analyzer
   *
   * @throws MetaverseAnalyzerException
   */
  protected void addConnectionNodes( IComponentDescriptor descriptor ) throws MetaverseAnalyzerException {

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

  public IComponentDescriptor getDescriptor() {
    return descriptor;
  }

  public void setDescriptor( IComponentDescriptor descriptor ) {
    this.descriptor = descriptor;
  }

  protected IMetaverseNode createFieldNode( IComponentDescriptor fieldDescriptor, ValueMetaInterface fieldMeta,
                                            String targetStepName, boolean addTheNode ) {

    IMetaverseNode newFieldNode = createNodeFromDescriptor( fieldDescriptor );
    newFieldNode.setProperty( DictionaryConst.PROPERTY_KETTLE_TYPE, fieldMeta.getTypeDesc() );

    // don't add it to the graph if it is a transient node
    if ( targetStepName != null ) {
      newFieldNode.setProperty( DictionaryConst.PROPERTY_TARGET_STEP, targetStepName );
      newFieldNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_TARGET_AWARE );
      if ( addTheNode ) {
        getMetaverseBuilder().addNode( newFieldNode );
      }
    }

    return newFieldNode;
  }

}

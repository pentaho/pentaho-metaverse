/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.entry;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobLineageHolderMap;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumer;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumerProvider;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.util.MetaverseBeanUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExtensionPoint(
  description = "Job entry external resource listener",
  extensionPointId = "JobBeforeJobEntryExecution",
  id = "jobEntryExternalResource" )
public class JobEntryExternalResourceConsumerListener implements ExtensionPointInterface {

  private IJobEntryExternalResourceConsumerProvider jobEntryConsumerProvider;

  /**
   * This method is called by the Kettle code when a job entry is about to start
   *
   * @param log    the logging channel to log debugging information to
   * @param object The subject object that is passed to the plugin code
   * @throws org.pentaho.di.core.exception.KettleException In case the plugin decides that an error has occurred
   *                                                       and the parent process should stop.
   */
  @Override
  public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    if ( jobEntryConsumerProvider == null ) {
      jobEntryConsumerProvider = (IJobEntryExternalResourceConsumerProvider)
        MetaverseBeanUtil.getInstance().get( IJobEntryExternalResourceConsumerProvider.class );
    }
    JobExecutionExtension jobExec = (JobExecutionExtension) object;
    JobEntryCopy jobEntryCopy = jobExec.jobEntryCopy;
    if ( jobEntryCopy != null ) {
      JobEntryInterface meta = jobEntryCopy.getEntry();
      if ( meta != null ) {
        Class<?> metaClass = meta.getClass();
        if ( JobEntryBase.class.isAssignableFrom( metaClass ) ) {
          if ( jobEntryConsumerProvider != null ) {
            // Put the class into a collection and get the consumers that can process this class
            Set<Class<?>> metaClassSet = new HashSet<Class<?>>( 1 );
            metaClassSet.add( metaClass );

            List<IJobEntryExternalResourceConsumer> jobEntryConsumers =
              jobEntryConsumerProvider.getExternalResourceConsumers( metaClassSet );
            if ( jobEntryConsumers != null ) {
              for ( IJobEntryExternalResourceConsumer jobEntryConsumer : jobEntryConsumers ) {
                // We might know enough at this point, so call the consumer
                Collection<IExternalResourceInfo> resources = jobEntryConsumer.getResourcesFromMeta( meta );
                addExternalResources( resources, meta );
                // Add a JobEntryListener to collect external resource info after a job entry has finished
                if ( jobExec.job != null && jobEntryConsumer.isDataDriven( meta ) ) {
                  // Add the consumer as a resource listener, this is done to override the default impl
                  if ( jobEntryConsumer instanceof JobEntryExternalResourceListener ) {
                    jobExec.job.addJobEntryListener( (JobEntryExternalResourceListener) jobEntryConsumer );
                  } else {
                    jobExec.job.addJobEntryListener( new JobEntryExternalResourceListener() );
                  }
                }
              }
            } else {
              // Add a JobEntryListener to collect external resource info after a job entry has finished
              if ( jobExec.job != null ) {
                jobExec.job.addJobEntryListener( new JobEntryExternalResourceListener() );
              }
            }
          }
        }
      }
    }
  }

  protected void addExternalResources( Collection<IExternalResourceInfo> resources, JobEntryInterface jobEntry ) {
    if ( resources != null ) {
      // Add the resources to the execution profile
      IExecutionProfile executionProfile =
        JobLineageHolderMap.getInstance().getLineageHolder( jobEntry.getParentJob() ).getExecutionProfile();
      if ( executionProfile != null ) {
        String jobEntryName = jobEntry.getName();
        Map<String, List<IExternalResourceInfo>> resourceMap =
          executionProfile.getExecutionData().getExternalResources();
        List<IExternalResourceInfo> externalResources = resourceMap.get( jobEntryName );
        if ( externalResources == null ) {
          externalResources = new LinkedList<IExternalResourceInfo>();
        }
        externalResources.addAll( resources );
        resourceMap.put( jobEntryName, externalResources );
      }
    }
  }

  public void setJobEntryExternalResourceConsumerProvider( IJobEntryExternalResourceConsumerProvider provider ) {
    this.jobEntryConsumerProvider = provider;
  }
}

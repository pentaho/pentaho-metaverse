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
package com.pentaho.metaverse.analyzer.kettle.extensionpoints.job.entry;

import com.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobRuntimeExtensionPoint;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumer;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumerProvider;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.util.MetaverseBeanUtil;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;

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
        MetaverseBeanUtil.getInstance().get( "IJobEntryExternalResourceConsumerProvider" );
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
        JobRuntimeExtensionPoint.getLineageHolder( jobEntry.getParentJob() ).getExecutionProfile();
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

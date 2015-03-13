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
package com.pentaho.metaverse.analyzer.kettle.extensionpoints.job.entry;

import com.pentaho.metaverse.analyzer.kettle.extensionpoints.ExternalResourceConsumerMap;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.IJobEntryExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobRuntimeExtensionPoint;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

@ExtensionPoint(
  description = "Job entry external resource listener",
  extensionPointId = "JobBeforeJobEntryExecution",
  id = "jobEntryExternalResource" )
public class JobEntryExternalResourceConsumerListener implements ExtensionPointInterface {


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
    JobExecutionExtension jobExec = (JobExecutionExtension) object;
    JobEntryCopy jobEntryCopy = jobExec.jobEntryCopy;
    if ( jobEntryCopy != null ) {
      JobEntryInterface meta = jobEntryCopy.getEntry();
      if ( meta != null ) {
        Class<?> metaClass = meta.getClass();
        if ( JobEntryBase.class.isAssignableFrom( metaClass ) ) {
          @SuppressWarnings( "unchecked" )
          Queue<IJobEntryExternalResourceConsumer> jobEntryConsumers =
            ExternalResourceConsumerMap.getInstance().getJobEntryExternalResourceConsumers(
              (Class<? extends JobEntryBase>) metaClass );
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

  protected void addExternalResources( Collection<IExternalResourceInfo> resources, JobEntryInterface jobEntry ) {
    if ( resources != null ) {
      // Add the resources to the execution profile
      IExecutionProfile executionProfile = JobRuntimeExtensionPoint.getProfileMap().get( jobEntry.getParentJob() );
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
}

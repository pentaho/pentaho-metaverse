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
import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class JobEntryExternalResourceConsumerListenerTest {
  @Test
  public void testCallJobEntryExtensionPoint() throws Exception {
    JobEntryExternalResourceConsumerListener jobEntryExtensionPoint =
      new JobEntryExternalResourceConsumerListener();
    JobExecutionExtension jobExec = mock( JobExecutionExtension.class );
    JobEntryBase jobEntryBase = mock( JobEntryBase.class, withSettings().extraInterfaces( JobEntryInterface.class ) );
    JobEntryInterface jobEntryInterface = (JobEntryInterface) jobEntryBase;
    JobEntryCopy jobEntryCopy = mock( JobEntryCopy.class );
    when( jobEntryCopy.getEntry() ).thenReturn( jobEntryInterface );
    jobExec.jobEntryCopy = jobEntryCopy;
    jobEntryExtensionPoint.callExtensionPoint( null, jobExec );

    // Adda consumer
    Map<Class<? extends JobEntryBase>, List<IJobEntryExternalResourceConsumer>> jobEntryConsumerMap =
      ExternalResourceConsumerMap.getInstance().getJobEntryConsumerMap();
    List<IJobEntryExternalResourceConsumer> consumers = new ArrayList<IJobEntryExternalResourceConsumer>();
    jobEntryConsumerMap.put( jobEntryBase.getClass(), consumers );
    jobEntryExtensionPoint.callExtensionPoint( null, jobExec );

    IJobEntryExternalResourceConsumer consumer = mock( IJobEntryExternalResourceConsumer.class );
    when( consumer.getResourcesFromMeta( Mockito.any() ) ).thenReturn( Collections.emptyList() );
    consumers.add( consumer );
    Job mockJob = mock( Job.class );
    when( jobEntryInterface.getParentJob() ).thenReturn( mockJob );
    jobExec.job = mockJob;
    jobEntryExtensionPoint.callExtensionPoint( null, jobExec );
    when( consumer.isDataDriven( Mockito.any() ) ).thenReturn( Boolean.TRUE );
    jobEntryExtensionPoint.callExtensionPoint( null, jobExec );
  }

  @Test
  public void testCallJobEntryAddExternalResources() {
    JobEntryExternalResourceConsumerListener stepExtensionPoint =
      new JobEntryExternalResourceConsumerListener();
    stepExtensionPoint.addExternalResources( null, null );
    JobEntryInterface mockJobEntry = mock( JobEntryInterface.class );
    Job mockJob = mock( Job.class );
    when( mockJobEntry.getParentJob() ).thenReturn( mockJob );

    IExecutionProfile executionProfile = mock( IExecutionProfile.class );
    IExecutionData executionData = mock( IExecutionData.class );
    when( executionProfile.getExecutionData() ).thenReturn( executionData );
    JobRuntimeExtensionPoint.getProfileMap().put( mockJob, executionProfile );

    Collection<IExternalResourceInfo> externalResources = new ArrayList<IExternalResourceInfo>();
    stepExtensionPoint.addExternalResources( externalResources, mockJobEntry );
    IExternalResourceInfo externalResource = mock( IExternalResourceInfo.class );
    externalResources.add( externalResource );
    stepExtensionPoint.addExternalResources( externalResources, mockJobEntry );
  }
}

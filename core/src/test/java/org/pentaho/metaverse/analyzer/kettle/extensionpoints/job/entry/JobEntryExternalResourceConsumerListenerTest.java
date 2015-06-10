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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.entry;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobRuntimeExtensionPoint;
import org.pentaho.metaverse.analyzer.kettle.jobentry.JobEntryExternalResourceConsumerProvider;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumer;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

public class JobEntryExternalResourceConsumerListenerTest {
  @Test
  public void testCallJobEntryExtensionPoint() throws Exception {
    JobEntryExternalResourceConsumerListener jobEntryExtensionPoint =
      new JobEntryExternalResourceConsumerListener();
    jobEntryExtensionPoint.setJobEntryExternalResourceConsumerProvider(
      MetaverseTestUtils.getJobEntryExternalResourceConsumerProvider() );
    JobExecutionExtension jobExec = mock( JobExecutionExtension.class );
    JobEntryBase jobEntryBase = mock( JobEntryBase.class, withSettings().extraInterfaces( JobEntryInterface.class ) );
    JobEntryInterface jobEntryInterface = (JobEntryInterface) jobEntryBase;
    JobEntryCopy jobEntryCopy = mock( JobEntryCopy.class );
    when( jobEntryCopy.getEntry() ).thenReturn( jobEntryInterface );
    jobExec.jobEntryCopy = jobEntryCopy;
    jobEntryExtensionPoint.callExtensionPoint( null, jobExec );

    // Adda consumer
    Map<Class<? extends JobEntryBase>, Set<IJobEntryExternalResourceConsumer>> jobEntryConsumerMap =
      new JobEntryExternalResourceConsumerProvider().getJobEntryConsumerMap();
    Set<IJobEntryExternalResourceConsumer> consumers = new HashSet<IJobEntryExternalResourceConsumer>();
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
    JobRuntimeExtensionPoint.getLineageHolder( mockJob).setExecutionProfile( executionProfile );

    Collection<IExternalResourceInfo> externalResources = new ArrayList<IExternalResourceInfo>();
    stepExtensionPoint.addExternalResources( externalResources, mockJobEntry );
    IExternalResourceInfo externalResource = mock( IExternalResourceInfo.class );
    externalResources.add( externalResource );
    stepExtensionPoint.addExternalResources( externalResources, mockJobEntry );
  }
}

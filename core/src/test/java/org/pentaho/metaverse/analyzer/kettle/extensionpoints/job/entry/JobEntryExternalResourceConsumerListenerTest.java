/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.entry;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobLineageHolderMap;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

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
    JobEntryExternalResourceConsumerProvider.clearInstance();
    Map<Class<? extends JobEntryBase>, Set<IJobEntryExternalResourceConsumer>> jobEntryConsumerMap =
      JobEntryExternalResourceConsumerProvider.getInstance().getJobEntryConsumerMap();
    Set<IJobEntryExternalResourceConsumer> consumers = new HashSet<IJobEntryExternalResourceConsumer>();
    jobEntryConsumerMap.put( jobEntryBase.getClass(), consumers );
    jobEntryExtensionPoint.callExtensionPoint( null, jobExec );


    IJobEntryExternalResourceConsumer consumer = mock( IJobEntryExternalResourceConsumer.class );
    when( consumer.getResourcesFromMeta( Mockito.any( Bowl.class ), Mockito.any( BaseStepMeta.class ) ) )
      .thenReturn( Collections.emptyList() );
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
    JobLineageHolderMap.getInstance().getLineageHolder( mockJob ).setExecutionProfile( executionProfile );

    Collection<IExternalResourceInfo> externalResources = new ArrayList<IExternalResourceInfo>();
    stepExtensionPoint.addExternalResources( externalResources, mockJobEntry );
    IExternalResourceInfo externalResource = mock( IExternalResourceInfo.class );
    externalResources.add( externalResource );
    stepExtensionPoint.addExternalResources( externalResources, mockJobEntry );
  }
}

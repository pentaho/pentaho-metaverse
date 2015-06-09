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

import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobRuntimeExtensionPoint;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobEntryExternalResourceListenerTest {

  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void testBeforeAfterExecution() throws Exception {
    IJobEntryExternalResourceConsumer consumer = mock( IJobEntryExternalResourceConsumer.class );
    JobMeta mockJobMeta = mock( JobMeta.class );
    Job job = mock( Job.class );
    when( job.getJobMeta() ).thenReturn( mockJobMeta );
    JobEntryInterface jobEntryInterface = mock( JobEntryInterface.class );
    when( jobEntryInterface.getParentJob() ).thenReturn( job );
    when( jobEntryInterface.getResourceDependencies( mockJobMeta ) ).thenReturn(
      Collections.singletonList( new ResourceReference( null,
        Collections.singletonList( new ResourceEntry( "myFile", ResourceEntry.ResourceType.FILE ) ) ) ) );
    JobEntryCopy jobEntryCopy = mock( JobEntryCopy.class );

    IExecutionProfile executionProfile = mock( IExecutionProfile.class );
    IExecutionData executionData = mock( IExecutionData.class );
    when( executionProfile.getExecutionData() ).thenReturn( executionData );
    JobRuntimeExtensionPoint.getLineageHolder( job ).setExecutionProfile( executionProfile );

    JobEntryExternalResourceListener listener = new JobEntryExternalResourceListener( consumer );

    FileObject mockFile = mock( FileObject.class );
    FileName mockFilename = mock( FileName.class );
    when( mockFilename.getPath() ).thenReturn( "/path/to/file" );
    when( mockFile.getName() ).thenReturn( mockFilename );
    ResultFile resultFile = mock( ResultFile.class );
    when( resultFile.getFile() ).thenReturn( mockFile );
    List<ResultFile> resultFiles = Collections.singletonList( resultFile );
    Result result = mock( Result.class );
    when( result.getResultFilesList() ).thenReturn( resultFiles );

    // Call beforeExecution for coverage
    listener.beforeExecution( null, null, null );

    listener.afterExecution( job, jobEntryCopy, jobEntryInterface, result );
  }
}

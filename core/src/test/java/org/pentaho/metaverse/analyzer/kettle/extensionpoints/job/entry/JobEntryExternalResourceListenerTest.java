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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
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
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobLineageHolderMap;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumer;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;

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
    JobLineageHolderMap.getInstance().getLineageHolder( job ).setExecutionProfile( executionProfile );

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

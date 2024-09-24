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

import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryListener;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobLineageHolderMap;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumer;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;

import java.util.List;

/**
 * JobEntryExternalResourceListener is a JobEntryListener that will record the external resources (files, e.g.) used
 * during a job entry's execution into that job's execution profile.
 */
public class JobEntryExternalResourceListener implements JobEntryListener {

  IJobEntryExternalResourceConsumer consumer;

  public JobEntryExternalResourceListener() {
  }

  public JobEntryExternalResourceListener( IJobEntryExternalResourceConsumer consumer ) {
    this.consumer = consumer;
  }

  @Override
  public void beforeExecution( Job job, JobEntryCopy jobEntryCopy, JobEntryInterface jobEntryInterface ) {
    // no-op, all pre-execution operations are performed by the extension point
  }

  @Override
  public void afterExecution( Job job, JobEntryCopy jobEntryCopy, JobEntryInterface jobEntryInterface, Result result ) {
    IExecutionProfile executionProfile =
      JobLineageHolderMap.getInstance().getLineageHolder( job ).getExecutionProfile();
    IExecutionData executionData = executionProfile.getExecutionData();

    // Get input files (aka Resource Dependencies)
    JobMeta jobMeta = job.getJobMeta();
    if ( jobMeta != null ) {
      List<ResourceReference> dependencies = jobEntryInterface.getResourceDependencies( jobMeta );
      if ( dependencies != null ) {
        for ( ResourceReference ref : dependencies ) {
          List<ResourceEntry> resourceEntries = ref.getEntries();
          if ( resourceEntries != null ) {
            for ( ResourceEntry entry : resourceEntries ) {
              executionData.addExternalResource( jobEntryInterface.getName(),
                ExternalResourceInfoFactory.createResource( entry, true ) );
            }
          }
        }
      }
    }

    // Get output files (aka result files)
    if ( result != null ) {
      List<ResultFile> resultFiles = result.getResultFilesList();
      if ( resultFiles != null ) {
        for ( ResultFile resultFile : resultFiles ) {
          executionData.addExternalResource( jobEntryInterface.getName(),
            ExternalResourceInfoFactory.createFileResource( resultFile.getFile(), false ) );
        }
      }
    }

  }
}

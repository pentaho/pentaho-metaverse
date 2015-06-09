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
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryListener;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;

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
    IExecutionProfile executionProfile = JobRuntimeExtensionPoint.getLineageHolder( job ).getExecutionProfile();
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

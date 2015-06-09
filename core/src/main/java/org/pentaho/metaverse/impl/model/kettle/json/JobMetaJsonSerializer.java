/*
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

package org.pentaho.metaverse.impl.model.kettle.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.metaverse.api.model.kettle.HopInfo;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;
import org.pentaho.metaverse.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * User: RFellows Date: 12/15/14
 */
public class JobMetaJsonSerializer extends AbstractMetaJsonSerializer<JobMeta> {
  private static final Logger LOGGER = LoggerFactory.getLogger( JobMetaJsonSerializer.class );

  public JobMetaJsonSerializer( Class<JobMeta> aClass ) {
    super( aClass );
  }

  public JobMetaJsonSerializer( JavaType javaType ) {
    super( javaType );
  }

  public JobMetaJsonSerializer( Class<?> aClass, boolean b ) {
    super( aClass, b );
  }

  @Override
  protected void serializeHops( JobMeta meta, JsonGenerator json ) throws IOException {
    // Hops
    json.writeArrayFieldStart( JSON_PROPERTY_HOPS );
    int numberOfHops = meta.nrJobHops();
    for ( int i = 0; i < numberOfHops; i++ ) {
      JobHopMeta hopMeta = meta.getJobHop( i );
      HopInfo hopInfo = new HopInfo( hopMeta );
      json.writeObject( hopInfo );
    }
    json.writeEndArray();
  }

  @Override
  protected void serializeSteps( JobMeta meta, JsonGenerator json ) throws IOException {
    json.writeArrayFieldStart( JSON_PROPERTY_STEPS );
    int numberOfEntries = meta.nrJobEntries();
    for ( int i = 0; i < numberOfEntries; i++ ) {
      JobEntryCopy jobEntry = meta.getJobEntry( i );
      LineageRepository repo = getLineageRepository();
      ObjectId jobId = meta.getObjectId() == null ? new StringObjectId( meta.getName() ) : meta.getObjectId();
      ObjectId entryId = jobEntry.getObjectId() == null
          ? new StringObjectId( jobEntry.getName() ) : jobEntry.getObjectId();

      JobEntryInterface jobEntryInterface = jobEntry.getEntry();
      JobEntryBase jobEntryBase = getJobEntryBase( jobEntryInterface );
      Job job = new Job( null, meta );
      jobEntryBase.setParentJob( job );
      jobEntryInterface.setObjectId( entryId );
      try {
        jobEntryInterface.saveRep( repo, null, jobId );
      } catch ( KettleException e ) {
        LOGGER.warn( Messages.getString( "INFO.Serialization.Trans.Step", jobEntry.getName() ), e );
      }
      json.writeObject( jobEntryBase );
    }
    json.writeEndArray();
  }

  protected JobEntryBase getJobEntryBase( JobEntryInterface jobEntryInterface ) {
    JobEntryBase jobEntryBase = new JobEntryBase( jobEntryInterface.getName(), jobEntryInterface.getDescription() );
    if ( jobEntryInterface instanceof JobEntryBase ) {
      jobEntryBase = (JobEntryBase) jobEntryInterface;
    }
    return jobEntryBase;
  }

  @Override
  protected List<String> getUsedVariables( JobMeta meta ) {
    return meta.getUsedVariables();
  }

}

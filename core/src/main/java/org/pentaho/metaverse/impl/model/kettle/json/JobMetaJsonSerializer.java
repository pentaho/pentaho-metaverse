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


package org.pentaho.metaverse.impl.model.kettle.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.annotations.VisibleForTesting;
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

  private static JobMetaJsonSerializer instance;

  private JobMetaJsonSerializer() {
    this( JobMeta.class );
    this.setLineageRepository( LineageRepository.getInstance() );
  }

  public static JobMetaJsonSerializer getInstance() {
    if ( null == instance ) {
      instance = new JobMetaJsonSerializer();
    }
    return instance;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger( JobMetaJsonSerializer.class );

  @VisibleForTesting
  JobMetaJsonSerializer( Class<JobMeta> aClass ) {
    super( aClass );
  }

  @VisibleForTesting
  JobMetaJsonSerializer( JavaType javaType ) {
    super( javaType );
  }

  @VisibleForTesting
  JobMetaJsonSerializer( Class<?> aClass, boolean b ) {
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

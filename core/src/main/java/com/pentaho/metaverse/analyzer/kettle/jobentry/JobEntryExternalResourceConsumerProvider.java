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
package com.pentaho.metaverse.analyzer.kettle.jobentry;


import org.pentaho.di.job.entry.JobEntryBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class maintains a map of JobEntryInterface classes to lists of JobEntryExternalResourceConsumers for the
 * purposes of fast lookups to record external resources being used by job entries
 */
public class JobEntryExternalResourceConsumerProvider implements IJobEntryExternalResourceConsumerProvider {

  private List<IJobEntryExternalResourceConsumer> jobEntryExternalResourceConsumers;

  private Map<Class<? extends JobEntryBase>, Set<IJobEntryExternalResourceConsumer>> jobEntryConsumerMap;


  public JobEntryExternalResourceConsumerProvider() {
    jobEntryConsumerMap =
      new ConcurrentHashMap<Class<? extends JobEntryBase>, Set<IJobEntryExternalResourceConsumer>>();
  }

  /**
   * Sets the external resource consumers for this provider
   *
   * @param jobEntryConsumers the consumers to set for this provider
   */
  public void setExternalResourceConsumers( List<IJobEntryExternalResourceConsumer> jobEntryConsumers ) {
    this.jobEntryExternalResourceConsumers = jobEntryConsumers;
    loadJobEntryExternalResourceConsumerMap();
  }

  /**
   * Return the set of external resource consumers for this type
   *
   * @return The external resources consumers provided by this object
   */
  @Override
  public List<IJobEntryExternalResourceConsumer> getExternalResourceConsumers() {
    return jobEntryExternalResourceConsumers;
  }

  /**
   * Return the set of external resource consumers for this type for a given set of classes
   *
   * @param types The set of classes to filter by
   * @return The external resource consumers of the specified types offered by this provider
   */
  @Override
  public List<IJobEntryExternalResourceConsumer> getExternalResourceConsumers( Collection<Class<?>> types ) {
    List<IJobEntryExternalResourceConsumer> jobEntryConsumers = getExternalResourceConsumers();
    if ( types != null ) {
      final Set<IJobEntryExternalResourceConsumer> specificJobEntryAnalyzers =
        new HashSet<IJobEntryExternalResourceConsumer>();
      for ( Class<?> clazz : types ) {
        if ( jobEntryConsumerMap.containsKey( clazz ) ) {
          specificJobEntryAnalyzers.addAll( jobEntryConsumerMap.get( clazz ) );
        }
      }
      jobEntryConsumers = new ArrayList<IJobEntryExternalResourceConsumer>( specificJobEntryAnalyzers );
    }
    return jobEntryConsumers;
  }

  /**
   * Adds an external resource consumer to group of supported consumers
   *
   * @param externalResourceConsumer the consumer to add
   */
  @Override
  public void addExternalResourceConsumer( IJobEntryExternalResourceConsumer externalResourceConsumer ) {
    if ( !jobEntryExternalResourceConsumers.contains( externalResourceConsumer ) ) {
      jobEntryExternalResourceConsumers.add( externalResourceConsumer );
    }

    Class<? extends JobEntryBase> metaClass = externalResourceConsumer.getMetaClass();
    if ( metaClass != null ) {
      Set<IJobEntryExternalResourceConsumer> consumerSet = null;
      if ( jobEntryConsumerMap.containsKey( metaClass ) ) {
        consumerSet = jobEntryConsumerMap.get( metaClass );
      } else {
        consumerSet = new HashSet<IJobEntryExternalResourceConsumer>();

      }
      consumerSet.add( externalResourceConsumer );
      jobEntryConsumerMap.put( metaClass, consumerSet );
    }
  }

  /**
   * Removes an externalResourceConsumer from the group of supported consumers
   *
   * @param externalResourceConsumer the consumer to remove
   */
  @Override
  public void removeExternalResourceConsumer( IJobEntryExternalResourceConsumer externalResourceConsumer ) {
    if ( jobEntryExternalResourceConsumers.contains( externalResourceConsumer ) ) {
      try {
        jobEntryExternalResourceConsumers.remove( externalResourceConsumer );
      } catch ( UnsupportedOperationException uoe ) {
        // This comes from Blueprint for managed containers (which are read-only). Nothing to do in this case
      }
    }

    if ( externalResourceConsumer != null ) {
      Class<? extends JobEntryBase> metaClass = externalResourceConsumer.getMetaClass();
      if ( metaClass != null ) {
        Set<IJobEntryExternalResourceConsumer> consumerSet = null;
        if ( jobEntryConsumerMap.containsKey( metaClass ) ) {
          consumerSet = jobEntryConsumerMap.get( metaClass );
          consumerSet.remove( externalResourceConsumer );
          if ( consumerSet.isEmpty() ) {
            jobEntryConsumerMap.remove( metaClass );
          }
        }
      }
    }
  }

  public Map<Class<? extends JobEntryBase>, Set<IJobEntryExternalResourceConsumer>> getJobEntryConsumerMap() {
    return jobEntryConsumerMap;
  }

  /**
   * Loads up a Map of document types to supporting IJobEntryAnalyzer(s)
   */
  protected void loadJobEntryExternalResourceConsumerMap() {
    jobEntryConsumerMap = new HashMap<Class<? extends JobEntryBase>, Set<IJobEntryExternalResourceConsumer>>();
    if ( jobEntryExternalResourceConsumers != null ) {
      for ( IJobEntryExternalResourceConsumer jobEntryConsumer : jobEntryExternalResourceConsumers ) {
        addExternalResourceConsumer( jobEntryConsumer );
      }
    }
  }
}

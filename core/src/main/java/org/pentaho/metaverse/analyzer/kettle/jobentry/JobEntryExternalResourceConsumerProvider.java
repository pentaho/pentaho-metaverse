/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.jobentry;


import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumer;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumerProvider;

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

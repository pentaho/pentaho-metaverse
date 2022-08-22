/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumerProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class maintains a map of StepMeta classes to lists of StepExternalResourceConsumers for the purposes
 * of fast lookups to record external resources being used by steps
 */
public class StepExternalResourceConsumerProvider implements IStepExternalResourceConsumerProvider {

  private List<IStepExternalResourceConsumer> stepConsumers;

  private Map<Class<? extends BaseStepMeta>, Set<IStepExternalResourceConsumer>> stepConsumerMap;

  private static StepExternalResourceConsumerProvider instance;

  public static StepExternalResourceConsumerProvider getInstance() {
    if ( null == instance ) {
      instance = new StepExternalResourceConsumerProvider();
    }
    return instance;
  }

  @VisibleForTesting
  public static void clearInstance() {
    instance = null;
  }

  @VisibleForTesting
  private StepExternalResourceConsumerProvider() {
    stepConsumerMap = new ConcurrentHashMap<>();
    stepConsumers = Collections.synchronizedList( new ArrayList<>() );
  }

  public void setExternalResourceConsumers( List<IStepExternalResourceConsumer> stepConsumers ) {
    this.stepConsumers = stepConsumers;
    loadStepExternalResourceConsumerMap();
  }

  /**
   * Return the set of external resource consumers for this type
   *
   * @return The analyzers
   */
  @Override
  public List<IStepExternalResourceConsumer> getExternalResourceConsumers() {
    if ( null == stepConsumers || stepConsumers.isEmpty() ) {
      stepConsumers = Collections.synchronizedList( new ArrayList<>() );
      // need to check this at runtime vs during startup in case external plugins have registered StepExternalResourceConsumers
      stepConsumers.addAll( PentahoSystem.getAll( IStepExternalResourceConsumer.class ) );
    }
    return stepConsumers;
  }

  /**
   * Return the set of external resource consumers for this type for a given set of classes
   *
   * @param types The set of classes to filter by
   * @return The external resource consumers
   */
  @Override
  public List<IStepExternalResourceConsumer> getExternalResourceConsumers( Collection<Class<?>> types ) {
    List<IStepExternalResourceConsumer> stepExternalResourceConsumers = getExternalResourceConsumers();
    if ( types != null ) {
      final Set<IStepExternalResourceConsumer> specificStepAnalyzers = new HashSet<>();
      for ( Class<?> clazz : types ) {
        if ( getStepConsumerMap().containsKey( clazz ) ) {
          specificStepAnalyzers.addAll( getStepConsumerMap().get( clazz ) );
        }
      }
      stepExternalResourceConsumers = new ArrayList<>( specificStepAnalyzers );
    }
    return stepExternalResourceConsumers;
  }

  /**
   * Adds an external resource consumer to group of supported consumers
   *
   * @param externalResourceConsumer
   */
  @Override
  public void addExternalResourceConsumer( IStepExternalResourceConsumer externalResourceConsumer ) {
    if ( !stepConsumers.contains( externalResourceConsumer ) ) {
      stepConsumers.add( externalResourceConsumer );
    }

    Class<? extends BaseStepMeta> metaClass = externalResourceConsumer.getMetaClass();
    if ( metaClass != null ) {
      Set<IStepExternalResourceConsumer> consumerSet = stepConsumerMap.computeIfAbsent( metaClass, k -> new HashSet<>() );
      consumerSet.add( externalResourceConsumer );
      stepConsumerMap.put( metaClass, consumerSet );
    }
  }

  /**
   * Removes an externalResourceConsumer from the group of supported consumers
   *
   * @param externalResourceConsumer
   */
  @Override
  public void removeExternalResourceConsumer( IStepExternalResourceConsumer externalResourceConsumer ) {
    if ( stepConsumers.contains( externalResourceConsumer ) ) {
      try {
        stepConsumers.remove( externalResourceConsumer );
      } catch ( UnsupportedOperationException uoe ) {
        // This comes from Blueprint for managed containers (which are read-only). Nothing to do in this case
      }
    }

    if ( externalResourceConsumer != null ) {
      Class<? extends BaseStepMeta> metaClass = externalResourceConsumer.getMetaClass();
      if ( metaClass != null ) {
        Set<IStepExternalResourceConsumer> consumerSet = null;
        if ( stepConsumerMap.containsKey( metaClass ) ) {
          consumerSet = stepConsumerMap.get( metaClass );
          consumerSet.remove( externalResourceConsumer );
          if ( consumerSet.isEmpty() ) {
            stepConsumerMap.remove( metaClass );
          }
        }
      }
    }
  }

  public Map<Class<? extends BaseStepMeta>, Set<IStepExternalResourceConsumer>> getStepConsumerMap() {
    if ( null == stepConsumerMap || stepConsumerMap.isEmpty() ) {
      stepConsumerMap = new ConcurrentHashMap<>();
      List<IStepExternalResourceConsumer> consumerList = getExternalResourceConsumers();
      for ( IStepExternalResourceConsumer consumer : consumerList ) {
        Class<? extends BaseStepMeta> metaClass = consumer.getMetaClass();
        if ( metaClass != null ) {
          Set<IStepExternalResourceConsumer> consumerSet = stepConsumerMap.computeIfAbsent( metaClass, k -> new HashSet<>() );
          consumerSet.add( consumer );
          stepConsumerMap.put( metaClass, consumerSet );
        }
      }
    }
    return stepConsumerMap;
  }

  /**
   * Loads up a Map of document types to supporting IStepAnalyzer(s)
   */
  protected void loadStepExternalResourceConsumerMap() {
    stepConsumerMap = getStepConsumerMap();
  }
}

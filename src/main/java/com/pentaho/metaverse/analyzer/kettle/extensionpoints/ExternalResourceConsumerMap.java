/*!
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
package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumerPluginType;
import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.step.BaseStepMeta;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class maintains a map of StepMeta classes to lists of ExternalResourceConsumers for the purposes
 * of fast lookups to record external resources being used by steps
 */
public class ExternalResourceConsumerMap {

  private static ExternalResourceConsumerMap INSTANCE = new ExternalResourceConsumerMap();

  private Map<Class<? extends BaseStepMeta>, List<IStepExternalResourceConsumer>> stepConsumerMap;

  private ExternalResourceConsumerMap() {
    stepConsumerMap = new ConcurrentHashMap<Class<? extends BaseStepMeta>, List<IStepExternalResourceConsumer>>();
  }

  public static ExternalResourceConsumerMap getInstance() {
    return INSTANCE;
  }

  public List<IStepExternalResourceConsumer> getStepExternalResourceConsumers(
      Class<? extends BaseStepMeta> stepMetaClass ) {
    return stepConsumerMap.get( stepMetaClass );
  }

  public Map<Class<? extends BaseStepMeta>, List<IStepExternalResourceConsumer>> getStepConsumerMap() {
    return stepConsumerMap;
  }

  @KettleLifecyclePlugin( id = "ExternalResourceConsumerMapBuilder", name = "ExternalResourceConsumerMapBuilder" )
  public static class ExternalResourceConsumerMapBuilder implements KettleLifecycleListener {
    @Override
    public void onEnvironmentInit() throws LifecycleException {
      try {
        PluginRegistry registry = PluginRegistry.getInstance();
        List<PluginInterface> consumerPlugins = registry.getPlugins( ExternalResourceConsumerPluginType.class );
        for ( PluginInterface plugin : consumerPlugins ) {
          Object o = registry.loadClass( plugin );
          if ( o instanceof IStepExternalResourceConsumer ) {
            IStepExternalResourceConsumer consumer = (IStepExternalResourceConsumer) o;
            Class<? extends BaseStepMeta> stepMetaClass = consumer.getStepMetaClass();
            Map<Class<? extends BaseStepMeta>, List<IStepExternalResourceConsumer>> stepConsumerMap =
                ExternalResourceConsumerMap.getInstance().getStepConsumerMap();
            List<IStepExternalResourceConsumer> stepMetaConsumers = stepConsumerMap.get( stepMetaClass );
            if ( stepMetaConsumers == null ) {
              stepMetaConsumers = new LinkedList<IStepExternalResourceConsumer>();
              stepConsumerMap.put( stepMetaClass, stepMetaConsumers );
            }
            stepMetaConsumers.add( consumer );
          }
        }
      } catch ( KettlePluginException kpe ) {
        throw new LifecycleException( kpe, true );
      }
    }

    @Override
    public void onEnvironmentShutdown() {
      // noop
    }
  }


}

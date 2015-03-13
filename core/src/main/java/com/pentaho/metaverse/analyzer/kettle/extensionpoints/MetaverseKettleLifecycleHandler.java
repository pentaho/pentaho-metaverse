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
import org.pentaho.di.core.plugins.PluginTypeListener;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.trans.step.BaseStepMeta;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * MetaverseKettleLifecycleHandler processes lifecycle events (startup, shutdown) in terms of managing the lineage
 * capabilities, such as creation of document controller(s), plugin map(s),
 */
@KettleLifecyclePlugin( id = "MetaverseKettleLifecycleHandler", name = "MetaverseKettleLifecycleHandler" )
public class MetaverseKettleLifecycleHandler implements KettleLifecycleListener, PluginTypeListener {

  @Override
  public void onEnvironmentInit() throws LifecycleException {
    // Set up a plugin listener to populate the map of steps to external resource consumer plugins
    PluginRegistry.getInstance().addPluginListener( ExternalResourceConsumerPluginType.class, this );
  }

  @Override
  public void onEnvironmentShutdown() {
    // noop
  }

  @Override
  public void pluginAdded( Object pluginInterface ) {

    try {
      Object o = PluginRegistry.getInstance().loadClass( (PluginInterface) pluginInterface );
      if ( o instanceof IStepExternalResourceConsumer ) {
        IStepExternalResourceConsumer consumer = (IStepExternalResourceConsumer) o;
        Class<? extends BaseStepMeta> stepMetaClass = consumer.getMetaClass();
        Map<Class<? extends BaseStepMeta>, Queue<IStepExternalResourceConsumer>> stepConsumerMap =
          ExternalResourceConsumerMap.getInstance().getStepConsumerMap();
        Queue<IStepExternalResourceConsumer> stepMetaConsumers = stepConsumerMap.get( stepMetaClass );
        if ( stepMetaConsumers == null ) {
          stepMetaConsumers = new ConcurrentLinkedQueue<IStepExternalResourceConsumer>();
          stepConsumerMap.put( stepMetaClass, stepMetaConsumers );
        }
        stepMetaConsumers.add( consumer );
      } else if ( o instanceof IJobEntryExternalResourceConsumer ) {
        IJobEntryExternalResourceConsumer consumer = (IJobEntryExternalResourceConsumer) o;
        Class<? extends JobEntryBase> jobMetaClass = consumer.getMetaClass();
        Map<Class<? extends JobEntryBase>, Queue<IJobEntryExternalResourceConsumer>> jobEntryConsumerMap =
          ExternalResourceConsumerMap.getInstance().getJobEntryConsumerMap();
        Queue<IJobEntryExternalResourceConsumer> jobEntryMetaConsumers = jobEntryConsumerMap.get( jobMetaClass );
        if ( jobEntryMetaConsumers == null ) {
          jobEntryMetaConsumers = new ConcurrentLinkedQueue<IJobEntryExternalResourceConsumer>();
          jobEntryConsumerMap.put( jobMetaClass, jobEntryMetaConsumers );
        }
        jobEntryMetaConsumers.add( consumer );
      }
    } catch ( KettlePluginException kpe ) {
      // Ignore this one since we can't instantiate it
      kpe.printStackTrace();
    }
  }

  @Override
  public void pluginRemoved( Object pluginInterface ) {
    try {
      // Create a new plugin instance here, but only to get the meta class and the plugin class. We'll use those to
      // find the existing entry in the map to remove.
      Object o = PluginRegistry.getInstance().loadClass( (PluginInterface) pluginInterface );
      if ( o instanceof IStepExternalResourceConsumer ) {
        IStepExternalResourceConsumer consumer = (IStepExternalResourceConsumer) o;
        Class<? extends BaseStepMeta> stepMetaClass = consumer.getMetaClass();
        Map<Class<? extends BaseStepMeta>, Queue<IStepExternalResourceConsumer>> stepConsumerMap =
          ExternalResourceConsumerMap.getInstance().getStepConsumerMap();
        Queue<IStepExternalResourceConsumer> stepMetaConsumers = stepConsumerMap.get( stepMetaClass );
        if ( stepMetaConsumers != null ) {
          for ( IStepExternalResourceConsumer stepMetaConsumer : stepMetaConsumers ) {
            if ( stepMetaConsumer.getClass().equals( consumer.getClass() ) ) {
              stepMetaConsumers.remove( stepMetaConsumer );
            }
          }
        }

      } else if ( o instanceof IJobEntryExternalResourceConsumer ) {
        IJobEntryExternalResourceConsumer consumer = (IJobEntryExternalResourceConsumer) o;
        Class<? extends JobEntryBase> jobMetaClass = consumer.getMetaClass();
        Map<Class<? extends JobEntryBase>, Queue<IJobEntryExternalResourceConsumer>> jobEntryConsumerMap =
          ExternalResourceConsumerMap.getInstance().getJobEntryConsumerMap();
        Queue<IJobEntryExternalResourceConsumer> jobEntryMetaConsumers = jobEntryConsumerMap.get( jobMetaClass );
        if ( jobEntryMetaConsumers != null ) {
          for ( IJobEntryExternalResourceConsumer jobEntryMetaConsumer : jobEntryMetaConsumers ) {
            if ( jobEntryMetaConsumer.getClass().equals( consumer.getClass() ) ) {
              jobEntryMetaConsumers.remove( jobEntryMetaConsumer );
            }
          }
        }
      }
    } catch ( KettlePluginException kpe ) {
      // Ignore this one since we can't instantiate it
    }
  }

  @Override
  public void pluginChanged( Object pluginInterface ) {
    try {
      // Create a new plugin instance here, we'll use it to get the meta class and the plugin instance if it exists.
      // Then we remove any existing one and put this new instance in.
      Object o = PluginRegistry.getInstance().loadClass( (PluginInterface) pluginInterface );
      if ( o instanceof IStepExternalResourceConsumer ) {
        IStepExternalResourceConsumer consumer = (IStepExternalResourceConsumer) o;
        Class<? extends BaseStepMeta> stepMetaClass = consumer.getMetaClass();
        Map<Class<? extends BaseStepMeta>, Queue<IStepExternalResourceConsumer>> stepConsumerMap =
          ExternalResourceConsumerMap.getInstance().getStepConsumerMap();
        Queue<IStepExternalResourceConsumer> stepMetaConsumers = stepConsumerMap.get( stepMetaClass );
        if ( stepMetaConsumers != null ) {
          for ( IStepExternalResourceConsumer stepMetaConsumer : stepMetaConsumers ) {
            if ( stepMetaConsumer.getClass().equals( consumer.getClass() ) ) {
              stepMetaConsumers.remove( stepMetaConsumer );
            }
          }
          stepMetaConsumers.add( consumer );
        }

      } else if ( o instanceof IJobEntryExternalResourceConsumer ) {
        IJobEntryExternalResourceConsumer consumer = (IJobEntryExternalResourceConsumer) o;
        Class<? extends JobEntryBase> jobMetaClass = consumer.getMetaClass();
        Map<Class<? extends JobEntryBase>, Queue<IJobEntryExternalResourceConsumer>> jobEntryConsumerMap =
          ExternalResourceConsumerMap.getInstance().getJobEntryConsumerMap();
        Queue<IJobEntryExternalResourceConsumer> jobEntryMetaConsumers = jobEntryConsumerMap.get( jobMetaClass );
        if ( jobEntryMetaConsumers != null ) {
          for ( IJobEntryExternalResourceConsumer jobEntryMetaConsumer : jobEntryMetaConsumers ) {
            if ( jobEntryMetaConsumer.getClass().equals( consumer.getClass() ) ) {
              jobEntryMetaConsumers.remove( jobEntryMetaConsumer );
            }
          }
          jobEntryMetaConsumers.add( consumer );
        }
      }
    } catch ( KettlePluginException kpe ) {
      // Ignore this one since we can't instantiate it
    }
  }
}

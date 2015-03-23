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
package com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.step;

import com.pentaho.metaverse.analyzer.kettle.step.IStepExternalResourceConsumerProvider;
import com.pentaho.metaverse.analyzer.kettle.step.IStepExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransformationRuntimeExtensionPoint;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.util.MetaverseBeanUtil;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExtensionPoint(
  description = "Step external resource listener",
  extensionPointId = "StepBeforeStart",
  id = "stepExternalResource" )
public class StepExternalResourceConsumerListener implements ExtensionPointInterface {

  private IStepExternalResourceConsumerProvider stepConsumerProvider;

  /**
   * This method is called by the Kettle code when a step is about to start
   *
   * @param log    the logging channel to log debugging information to
   * @param object The subject object that is passed to the plugin code
   * @throws org.pentaho.di.core.exception.KettleException In case the plugin decides that an error has occurred
   *                                                       and the parent process should stop.
   */
  @Override
  public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    if ( stepConsumerProvider == null ) {
      stepConsumerProvider = (IStepExternalResourceConsumerProvider)
        MetaverseBeanUtil.getInstance().get( "IStepExternalResourceConsumerProvider" );
    }
    StepMetaDataCombi stepCombi = (StepMetaDataCombi) object;
    if ( stepCombi != null ) {
      StepMetaInterface meta = stepCombi.meta;
      StepInterface step = stepCombi.step;

      if ( meta != null ) {
        Class<?> metaClass = meta.getClass();
        if ( BaseStepMeta.class.isAssignableFrom( metaClass ) ) {
          if ( stepConsumerProvider != null ) {
            // Put the class into a collection and get the consumers that can process this class
            Set<Class<?>> metaClassSet = new HashSet<Class<?>>( 1 );
            metaClassSet.add( metaClass );

            List<IStepExternalResourceConsumer> stepConsumers =
              stepConsumerProvider.getExternalResourceConsumers( metaClassSet );
            if ( stepConsumers != null ) {
              for ( IStepExternalResourceConsumer stepConsumer : stepConsumers ) {
                // We might know enough at this point, so call the consumer
                Collection<IExternalResourceInfo> resources = stepConsumer.getResourcesFromMeta( meta );
                addExternalResources( resources, step );

                // Add a RowListener if the step is data-driven
                if ( stepConsumer.isDataDriven( meta ) ) {
                  stepCombi.step.addRowListener(
                    new StepExternalConsumerRowListener( stepConsumer, step ) );
                }
              }
            }
          }
        }
      }
    }
  }

  protected void addExternalResources( Collection<IExternalResourceInfo> resources, StepInterface step ) {
    if ( resources != null ) {
      // Add the resources to the execution profile
      IExecutionProfile executionProfile = TransformationRuntimeExtensionPoint.getProfileMap().get( step.getTrans() );
      if ( executionProfile != null ) {
        String stepName = step.getStepname();
        Map<String, List<IExternalResourceInfo>> resourceMap =
          executionProfile.getExecutionData().getExternalResources();
        List<IExternalResourceInfo> externalResources = resourceMap.get( stepName );
        if ( externalResources == null ) {
          externalResources = new LinkedList<IExternalResourceInfo>();
        }
        externalResources.addAll( resources );
        resourceMap.put( stepName, externalResources );
      }
    }
  }

  public void setStepExternalResourceConsumerProvider( IStepExternalResourceConsumerProvider provider ) {
    this.stepConsumerProvider = provider;
  }
}

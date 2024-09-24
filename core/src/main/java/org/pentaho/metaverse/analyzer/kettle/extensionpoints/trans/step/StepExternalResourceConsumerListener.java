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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.step;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransLineageHolderMap;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumerProvider;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.util.MetaverseBeanUtil;

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
        MetaverseBeanUtil.getInstance().get( IStepExternalResourceConsumerProvider.class );
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
      IExecutionProfile executionProfile =
        TransLineageHolderMap.getInstance().getLineageHolder( step.getTrans() ).getExecutionProfile();
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

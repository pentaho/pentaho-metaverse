/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransformationRuntimeExtensionPoint;
import org.pentaho.metaverse.analyzer.kettle.step.StepExternalResourceConsumerProvider;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Created by mburgess on 12/11/14.
 */
public class StepExternalResourceConsumerListenerTest {

  @Test
  public void testCallStepExtensionPoint() throws Exception {
    StepExternalResourceConsumerListener stepExtensionPoint = new StepExternalResourceConsumerListener();
    stepExtensionPoint.setStepExternalResourceConsumerProvider(
      MetaverseTestUtils.getStepExternalResourceConsumerProvider() );
    StepMetaDataCombi stepCombi = mock( StepMetaDataCombi.class );
    BaseStepMeta bsm = mock( BaseStepMeta.class, withSettings().extraInterfaces( StepMetaInterface.class ) );
    stepCombi.meta = (StepMetaInterface) bsm;
    stepCombi.step = mock( StepInterface.class );
    stepCombi.stepMeta = mock( StepMeta.class );

    stepExtensionPoint.callExtensionPoint( null, stepCombi );
    Map<Class<? extends BaseStepMeta>, Set<IStepExternalResourceConsumer>> stepConsumerMap =
      new StepExternalResourceConsumerProvider().getStepConsumerMap();
    Set<IStepExternalResourceConsumer> consumers = new HashSet<IStepExternalResourceConsumer>();
    stepConsumerMap.put( bsm.getClass(), consumers );
    stepExtensionPoint.callExtensionPoint( null, stepCombi );
    IStepExternalResourceConsumer consumer = mock( IStepExternalResourceConsumer.class );
    when( consumer.getResourcesFromMeta( Mockito.any() ) ).thenReturn( Collections.emptyList() );
    consumers.add( consumer );
    Trans mockTrans = mock( Trans.class );
    when( stepCombi.step.getTrans() ).thenReturn( mockTrans );
    stepExtensionPoint.callExtensionPoint( null, stepCombi );
    when( consumer.isDataDriven( Mockito.any() ) ).thenReturn( Boolean.TRUE );
    stepExtensionPoint.callExtensionPoint( null, stepCombi );
  }

  @Test
  public void testCallStepAddExternalResources() {
    StepExternalResourceConsumerListener stepExtensionPoint =
      new StepExternalResourceConsumerListener();
    stepExtensionPoint.addExternalResources( null, null );
    StepInterface mockStep = mock( StepInterface.class );
    Trans mockTrans = mock( Trans.class );
    when( mockStep.getTrans() ).thenReturn( mockTrans );

    IExecutionProfile executionProfile = mock( IExecutionProfile.class );
    IExecutionData executionData = mock( IExecutionData.class );
    when( executionProfile.getExecutionData() ).thenReturn( executionData );
    LineageHolder holder = new LineageHolder();
    holder.setExecutionProfile( executionProfile );
    TransformationRuntimeExtensionPoint.putLineageHolder( mockTrans, holder );

    Collection<IExternalResourceInfo> externalResources = new ArrayList<IExternalResourceInfo>();
    stepExtensionPoint.addExternalResources( externalResources, mockStep );
    IExternalResourceInfo externalResource = mock( IExternalResourceInfo.class );
    externalResources.add( externalResource );
    stepExtensionPoint.addExternalResources( externalResources, mockStep );
  }
}

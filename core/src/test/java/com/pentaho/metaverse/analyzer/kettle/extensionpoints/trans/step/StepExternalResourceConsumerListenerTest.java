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
package com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.step;

import com.pentaho.metaverse.analyzer.kettle.step.StepExternalResourceConsumerProvider;
import com.pentaho.metaverse.analyzer.kettle.step.IStepExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransformationRuntimeExtensionPoint;
import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.api.model.LineageHolder;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

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

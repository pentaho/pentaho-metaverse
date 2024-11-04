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


package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.step;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransLineageHolderMap;
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
    StepExternalResourceConsumerProvider.clearInstance();
    Map<Class<? extends BaseStepMeta>, Set<IStepExternalResourceConsumer>> stepConsumerMap =
      StepExternalResourceConsumerProvider.getInstance().getStepConsumerMap();
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
    TransLineageHolderMap.getInstance().putLineageHolder( mockTrans, holder );

    Collection<IExternalResourceInfo> externalResources = new ArrayList<IExternalResourceInfo>();
    stepExtensionPoint.addExternalResources( externalResources, mockStep );
    IExternalResourceInfo externalResource = mock( IExternalResourceInfo.class );
    externalResources.add( externalResource );
    stepExtensionPoint.addExternalResources( externalResources, mockStep );
  }
}

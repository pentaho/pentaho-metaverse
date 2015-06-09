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

import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransformationRuntimeExtensionPoint;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.junit.Test;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class StepExternalConsumerRowListenerTest {

  @Test
  public void testStepExternalConsumerRowListener() throws Exception {
    IStepExternalResourceConsumer consumer = mock( IStepExternalResourceConsumer.class );
    BaseStep mockStep = mock( BaseStep.class, withSettings().extraInterfaces( StepInterface.class ) );
    StepMeta mockStepMeta = mock( StepMeta.class );
    BaseStepMeta bsm = mock( BaseStepMeta.class, withSettings().extraInterfaces( StepMetaInterface.class ) );
    StepMetaInterface stepMetaInterface = (StepMetaInterface) bsm;
    when( mockStep.getStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( stepMetaInterface );
    Trans mockTrans = mock( Trans.class );
    when( mockStep.getTrans() ).thenReturn( mockTrans );

    IExecutionProfile executionProfile = mock( IExecutionProfile.class );
    IExecutionData executionData = mock( IExecutionData.class );
    when( executionProfile.getExecutionData() ).thenReturn( executionData );
    LineageHolder holder = new LineageHolder(  );
    holder.setExecutionProfile( executionProfile );
    TransformationRuntimeExtensionPoint.putLineageHolder( mockTrans, holder );

    StepExternalConsumerRowListener listener = new StepExternalConsumerRowListener( consumer, mockStep );

    RowMetaInterface rmi = mock( RowMetaInterface.class );
    Object[] row = new Object[0];

    listener.rowReadEvent( rmi, row );
  }

}

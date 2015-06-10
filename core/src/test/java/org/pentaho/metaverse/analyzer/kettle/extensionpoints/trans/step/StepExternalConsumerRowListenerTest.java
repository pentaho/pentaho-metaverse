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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransformationRuntimeExtensionPoint;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;

import static org.mockito.Mockito.*;

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

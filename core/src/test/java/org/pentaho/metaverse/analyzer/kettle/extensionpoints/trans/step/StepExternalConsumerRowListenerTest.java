/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransLineageHolderMap;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.impl.model.ExecutionProfile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class StepExternalConsumerRowListenerTest {

  @Test
  public void testStepExternalConsumerRowListener() throws Exception {

    BaseStep mockStep = mock( BaseStep.class, withSettings().extraInterfaces( StepInterface.class ) );
    when( mockStep.getStepname() ).thenReturn( "my_step" );
    StepMeta mockStepMeta = mock( StepMeta.class );
    BaseStepMeta bsm = mock( BaseStepMeta.class, withSettings().extraInterfaces( StepMetaInterface.class ) );
    StepMetaInterface stepMetaInterface = (StepMetaInterface) bsm;
    when( mockStep.getStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( stepMetaInterface );
    Trans mockTrans = mock( Trans.class );
    when( mockStep.getTrans() ).thenReturn( mockTrans );

    LineageHolder holder = TransLineageHolderMap.getInstance().getLineageHolder( mockStep.getTrans() );
    IExecutionProfile executionProfile = new ExecutionProfile();

    holder.setExecutionProfile( executionProfile );
    TransLineageHolderMap.getInstance().putLineageHolder( mockTrans, holder );

    IStepExternalResourceConsumer consumer = mock( IStepExternalResourceConsumer.class );

    RowMetaInterface rmi = mock( RowMetaInterface.class );
    Object[] row1 = new String[] { "val1a", "val1b" };
    Object[] row2 = new String[] { "val21a", "val2b" };

    StepExternalConsumerRowListener listener = new StepExternalConsumerRowListener( consumer, mockStep );

    IExternalResourceInfo resource1 = mock( IExternalResourceInfo.class );
    IExternalResourceInfo resource2 = mock( IExternalResourceInfo.class );
    IExternalResourceInfo resource3 = mock( IExternalResourceInfo.class );
    when( consumer.getResourcesFromRow( mockStep, rmi, row1 ) ).thenReturn(
      Arrays.asList( new IExternalResourceInfo[] { resource1, resource2 } ) );
    when( consumer.getResourcesFromRow( mockStep, rmi, row2 ) ).thenReturn(
      Arrays.asList( new IExternalResourceInfo[] { resource2, resource3 } ) );

    listener.rowReadEvent( rmi, row1 );
    Map resourceMap = TransLineageHolderMap.getInstance().getLineageHolder( mockTrans ).getExecutionProfile()
      .getExecutionData().getExternalResources();
    Assert.assertNotNull( resourceMap );
    Assert.assertEquals( 1, resourceMap.size() );
    Assert.assertNotNull( resourceMap.get( "my_step" ) );
    Assert.assertTrue( resourceMap.get( "my_step" ) instanceof List );
    List resources = (List) resourceMap.get( "my_step" );
    Assert.assertNotNull( resources );
    Assert.assertEquals( 2, resources.size() );
    Assert.assertTrue( resources.contains( resource1 ) );
    Assert.assertTrue( resources.contains( resource2 ) );

    listener.rowReadEvent( rmi, row2 );
    resourceMap = TransLineageHolderMap.getInstance().getLineageHolder( mockTrans ).getExecutionProfile()
      .getExecutionData().getExternalResources();
    Assert.assertNotNull( resourceMap );
    Assert.assertEquals( 1, resourceMap.size() );
    Assert.assertNotNull( resourceMap.get( "my_step" ) );
    Assert.assertTrue( resourceMap.get( "my_step" ) instanceof List );
    resources = (List) resourceMap.get( "my_step" );
    Assert.assertNotNull( resources );
    Assert.assertEquals( 3, resources.size() );
    Assert.assertTrue( resources.contains( resource1 ) );
    Assert.assertTrue( resources.contains( resource2 ) );
    Assert.assertTrue( resources.contains( resource3 ) );
  }
}

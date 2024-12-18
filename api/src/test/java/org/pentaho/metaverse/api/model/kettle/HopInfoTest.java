/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.api.model.kettle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.step.StepMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * User: RFellows Date: 12/3/14
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class HopInfoTest {

  HopInfo hopInfo;
  @Mock TransHopMeta hopMeta;
  @Mock StepMeta fromStep;
  @Mock StepMeta toStep;

  @Before
  public void setUp() throws Exception {
    hopInfo = new HopInfo();

    when( hopMeta.getFromStep() ).thenReturn( fromStep );
    when( hopMeta.getToStep() ).thenReturn( toStep );
    when( hopMeta.isEnabled() ).thenReturn( false );
    when( fromStep.getName() ).thenReturn( "from" );
    when( toStep.getName() ).thenReturn( "to" );
  }

  @Test
  public void testConstructor_TransHopMeta() throws Exception {
    hopInfo = new HopInfo( hopMeta );
    assertEquals( fromStep.getName(), hopInfo.getFromStepName() );
    assertEquals( toStep.getName(), hopInfo.getToStepName() );
    assertFalse( hopInfo.isEnabled() );
  }

  @Test
  public void testGettersSetters() throws Exception {
    assertTrue( hopInfo.isEnabled() );
    hopInfo.setEnabled( false );
    assertFalse( hopInfo.isEnabled() );

    hopInfo.setFromStepName( "step from" );
    assertEquals( "step from", hopInfo.getFromStepName() );

    hopInfo.setToStepName( "step to" );
    assertEquals( "step to", hopInfo.getToStepName() );

    hopInfo.setType( "type" );
    assertEquals( "type", hopInfo.getType() );
  }
}

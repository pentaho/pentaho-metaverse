/*
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

package org.pentaho.metaverse.api.model.kettle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.step.StepMeta;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * User: RFellows Date: 12/3/14
 */
@RunWith( MockitoJUnitRunner.class )
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

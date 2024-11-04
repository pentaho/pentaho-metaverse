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


package org.pentaho.metaverse.analyzer.kettle.extensionpoints;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.analyzer.kettle.step.StepExternalResourceConsumerProvider;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class StepExternalResourceConsumerProviderTest {

  StepExternalResourceConsumerProvider stepExternalResourceConsumerProvider;

  @Before
  public void setUp() throws Exception {
    StepExternalResourceConsumerProvider.clearInstance();
    stepExternalResourceConsumerProvider = StepExternalResourceConsumerProvider.getInstance();
  }

  @Test
  public void testGetInstance() throws Exception {
    assertNotNull( stepExternalResourceConsumerProvider );
  }

  @Test
  public void testGetStepExternalResourceConsumers() throws Exception {
    Set<Class<?>> metaClassSet = new HashSet<Class<?>>( 1 );
    metaClassSet.add( BaseStepMeta.class );
    assertNotNull( stepExternalResourceConsumerProvider.getExternalResourceConsumers( metaClassSet ) );
  }

}

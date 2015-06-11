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
    stepExternalResourceConsumerProvider = new StepExternalResourceConsumerProvider();
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

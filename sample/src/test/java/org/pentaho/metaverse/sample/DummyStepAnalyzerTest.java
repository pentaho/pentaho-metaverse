/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.sample;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.metaverse.api.IMetaverseNode;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class DummyStepAnalyzerTest {

  DummyStepAnalyzer analyzer;

  @Mock
  DummyTransMeta meta;
  @Mock
  IMetaverseNode node;

  @Before
  public void setUp() throws Exception {
    analyzer = new DummyStepAnalyzer();
  }

  @Test
  public void testGetUsedFields() throws Exception {
    assertNull( analyzer.getUsedFields( meta ) );
  }

  @Test
  public void testCustomAnalyze() throws Exception {
    analyzer.customAnalyze( meta, node );

    // make sure the do_nothing property was set on the node, and it's value is true
    verify( node, times( 1 ) ).setProperty( "do_nothing", true );
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    Set<Class<? extends BaseStepMeta>> supportedSteps = analyzer.getSupportedSteps();
    assertNotNull( supportedSteps );
    assertEquals( 1, supportedSteps.size() );
    assertEquals( DummyTransMeta.class, supportedSteps.iterator().next() );
  }
}

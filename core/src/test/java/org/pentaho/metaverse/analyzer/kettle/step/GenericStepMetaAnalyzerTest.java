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

package org.pentaho.metaverse.analyzer.kettle.step;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.api.IMetaverseNode;

import static org.junit.Assert.assertNull;

/**
 * @author mburgess
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class)
public class GenericStepMetaAnalyzerTest {

  GenericStepMetaAnalyzer analyzer;

  @Mock
  private BaseStepMeta mockBaseStepMeta;
  @Mock
  IMetaverseNode node;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    analyzer = new GenericStepMetaAnalyzer();
  }

  @Test
  public void testGetSupportedSteps() {
    assertNull( analyzer.getSupportedSteps() );
  }

  @Test
  public void testGetUsedFields() throws Exception {
    assertNull( analyzer.getUsedFields( mockBaseStepMeta ) );
  }

  @Test
  public void testCustomAnalyze() throws Exception {
    analyzer.customAnalyze( mockBaseStepMeta, node );
  }
}

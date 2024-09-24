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

package org.pentaho.metaverse.analyzer.kettle.step.rowstoresult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.rowstoresult.RowsToResultMeta;
import org.pentaho.metaverse.analyzer.kettle.step.ClonableStepAnalyzerTest;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by rfellows on 4/3/15.
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class RowsToResultStepAnalyzerTest extends ClonableStepAnalyzerTest {

  protected RowsToResultStepAnalyzer analyzer;

  @Mock RowsToResultMeta meta;
  @Mock IMetaverseNode node;

  @Before
  public void setUp() throws Exception {
    analyzer = new RowsToResultStepAnalyzer();
  }

  @Test
  public void testGetUsedFields() throws Exception {
    assertNull( analyzer.getUsedFields( meta ) );
  }

  @Test
  public void testCustomAnalyze() throws Exception {
    // no custom logic, just call it for code coverage
    analyzer.customAnalyze( meta, node );
  }

  @Test
  public void testGetSupportedSteps() {
    RowsToResultStepAnalyzer analyzer = new RowsToResultStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( RowsToResultMeta.class ) );
  }

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new RowsToResultStepAnalyzer();
  }
}

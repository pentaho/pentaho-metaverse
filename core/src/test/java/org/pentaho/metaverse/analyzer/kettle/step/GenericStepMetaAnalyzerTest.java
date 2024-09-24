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

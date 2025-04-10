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


package org.pentaho.metaverse.analyzer.kettle.step;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;

import static org.junit.Assert.assertNotEquals;

public abstract class ClonableStepAnalyzerTest {

  protected abstract IClonableStepAnalyzer newInstance();

  @Test
  public void testCloneAnalyzer() {
    final IClonableStepAnalyzer analyzer = newInstance();
    // verify that cloneAnalyzer returns an instance that is different from the original
    assertNotEquals( analyzer, analyzer.cloneAnalyzer() );

  }
}

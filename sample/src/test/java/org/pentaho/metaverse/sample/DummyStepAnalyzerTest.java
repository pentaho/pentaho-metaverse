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

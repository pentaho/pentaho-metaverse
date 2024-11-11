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


package org.pentaho.metaverse.analyzer.kettle.step.csvfileinput;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.step.ClonableStepAnalyzerTest;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class CsvFileInputStepAnalyzerTest extends ClonableStepAnalyzerTest {

  private CsvFileInputStepAnalyzer analyzer;

  @Mock CsvInputMeta meta;
  @Mock INamespace mockNamespace;
  IComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
    analyzer = spy( new CsvFileInputStepAnalyzer() );
    analyzer.setDescriptor( descriptor );
    analyzer.setObjectFactory( MetaverseTestUtils.getMetaverseObjectFactory() );
  }

  @Test
  public void testGetUsedFields() throws Exception {
    assertNull( analyzer.getUsedFields( meta ) );
  }

  @Test
  public void testGetResourceInputNodeType() throws Exception {
    assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, analyzer.getResourceInputNodeType() );
  }

  @Test
  public void testGetResourceOutputNodeType() throws Exception {
    assertNull( analyzer.getResourceOutputNodeType() );
  }

  @Test
  public void testIsOutput() throws Exception {
    assertFalse( analyzer.isOutput() );
  }

  @Test
  public void testIsInput() throws Exception {
    assertTrue( analyzer.isInput() );
  }

  @Test
  public void testGetSupportedSteps() {
    CsvFileInputStepAnalyzer analyzer = new CsvFileInputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( CsvInputMeta.class ) );
  }

  @Test
  public void testCreateResourceNode() throws Exception {
    IExternalResourceInfo res = mock( IExternalResourceInfo.class );
    when( res.getName() ).thenReturn( "file:///Users/home/tmp/xyz.ktr" );
    IMetaverseNode resourceNode = analyzer.createResourceNode( res );
    assertNotNull( resourceNode );
    assertEquals( DictionaryConst.NODE_TYPE_FILE, resourceNode.getType() );
  }

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new CsvFileInputStepAnalyzer();
  }
}

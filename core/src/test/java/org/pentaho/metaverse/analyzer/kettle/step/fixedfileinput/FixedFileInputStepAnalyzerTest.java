/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

package org.pentaho.metaverse.analyzer.kettle.step.fixedfileinput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.fixedinput.FixedInput;
import org.pentaho.di.trans.steps.fixedinput.FixedInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class FixedFileInputStepAnalyzerTest {

  private FixedFileInputStepAnalyzer analyzer;

  @Mock FixedInputMeta meta;
  @Mock INamespace mockNamespace;
  @Mock FixedInput input;
  @Mock TransMeta transMeta;
  @Mock RowMetaInterface rmi;

  IComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
    analyzer = spy( new FixedFileInputStepAnalyzer() );
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
    FixedFileInputStepAnalyzer analyzer = new FixedFileInputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( FixedInputMeta.class ) );
  }

  @Test
  public void testFixedFileInputExternalResourceConsumer_nullFileName() throws Exception {
    FixedFileInputExternalResourceConsumer consumer = new FixedFileInputExternalResourceConsumer();

    StepMeta spyMeta = spy( new StepMeta( "test", meta ) );

    when( meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( meta.getFilename() ).thenReturn( null );
    
    assertFalse( consumer.isDataDriven( meta ) );
    assertTrue( consumer.getResourcesFromMeta( meta ).isEmpty() );

    when( rmi.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenThrow( KettleException.class );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromRow( input, rmi, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( FixedInputMeta.class, consumer.getMetaClass() );
  }

  @Test
  public void testFixedFileInputExternalResourceConsumer() throws Exception {
    FixedFileInputExternalResourceConsumer consumer = new FixedFileInputExternalResourceConsumer();

    StepMeta spyMeta = spy( new StepMeta( "test", meta ) );

    when( meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( meta.getFilename() ).thenReturn( "path/to/file.txt" );

    assertFalse( consumer.isDataDriven( meta ) );
    assertFalse( consumer.getResourcesFromMeta( meta ).isEmpty() );

    when( rmi.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenThrow( KettleException.class );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromRow( input, rmi, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( FixedInputMeta.class, consumer.getMetaClass() );
  }
}

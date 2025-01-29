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


package org.pentaho.metaverse.analyzer.kettle.step.fixedfileinput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.fixedinput.FixedInput;
import org.pentaho.di.trans.steps.fixedinput.FixedInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.step.ClonableStepAnalyzerTest;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class FixedFileInputStepAnalyzerTest extends ClonableStepAnalyzerTest {

  private FixedFileInputStepAnalyzer analyzer;

  @Mock FixedInputMeta meta;
  @Mock INamespace mockNamespace;
  @Mock FixedInput input;
  @Mock TransMeta transMeta;
  @Mock RowMetaInterface rmi;

  IComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
    analyzer = spy( new FixedFileInputStepAnalyzer() );
    analyzer.setDescriptor( descriptor );
    IMetaverseBuilder builder = mock( IMetaverseBuilder.class );
    analyzer.setMetaverseBuilder( builder );
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
    assertTrue( consumer.getResourcesFromMeta( DefaultBowl.getInstance(), meta ).isEmpty() );

    lenient().when( rmi.getString( Mockito.any(), Mockito.any(), Mockito.any() ) )
      .thenThrow( KettleValueException.class );
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
    assertFalse( consumer.getResourcesFromMeta( DefaultBowl.getInstance(), meta ).isEmpty() );

    lenient().when( rmi.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenThrow( KettleValueException.class );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromRow( input, rmi, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( FixedInputMeta.class, consumer.getMetaClass() );
  }

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new FixedFileInputStepAnalyzer();
  }
}

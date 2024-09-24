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

package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.file.BaseFileMeta;
import org.pentaho.metaverse.api.analyzer.kettle.IExternalResourceConsumer;

import java.util.Collection;

import static org.junit.Assert.*;

public class BaseStepExternalResourceConsumerTest {

  BaseStepExternalResourceConsumer consumer;

  @Before
  public void setUp() throws Exception {
    consumer = new BaseStepExternalResourceConsumer() {
      @Override
      public Class getMetaClass() {
        return null;
      }
    };
  }

  @Test
  public void testIsDataDriven() throws Exception {
    assertFalse( consumer.isDataDriven( null ) );
  }

  @Test
  public void testGetResourcesFromMeta() throws Exception {

    Collection<IExternalResourceConsumer> resources = null;
    BaseStepMeta meta = null;

    // null meta
    resources = consumer.getResourcesFromMeta( meta );
    assertNotNull( resources );
    assertTrue( resources.isEmpty() );

    // non-file meta
    meta = Mockito.mock( BaseStepMeta.class );
    resources = consumer.getResourcesFromMeta( meta );
    assertNotNull( resources );
    assertTrue( resources.isEmpty() );
    Mockito.verify( meta, Mockito.times( 0 ) ).getParentStepMeta();

    // file meta
    meta = Mockito.mock( BaseFileMeta.class );
    TransMeta transMeta = Mockito.mock( TransMeta.class );
    StepMeta stepMeta = Mockito.mock( StepMeta.class );
    Mockito.when( ( (BaseFileMeta) meta ).writesToFile() ).thenReturn( true );
    Mockito.when( meta.getParentStepMeta() ).thenReturn( stepMeta );
    Mockito.when( stepMeta.getParentTransMeta() ).thenReturn( transMeta );
    consumer.getResourcesFromMeta( meta );
    Mockito.verify( meta, Mockito.times( 2 ) ).getParentStepMeta();
    Mockito.verify( (BaseFileMeta) meta, Mockito.times( 1 ) ).getFilePaths( false );
  }

  @Test
  public void testGetResourcesFromRow() throws Exception {
    Collection<IExternalResourceConsumer> resources = consumer.getResourcesFromRow( null, null, null );
    assertNotNull( resources );
    assertTrue( resources.isEmpty() );
  }
}

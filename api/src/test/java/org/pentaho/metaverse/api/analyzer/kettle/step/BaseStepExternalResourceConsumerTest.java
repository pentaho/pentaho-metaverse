/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.trans.step.BaseStepMeta;
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
    Mockito.when( ( (BaseFileMeta) meta ).writesToFile() ).thenReturn( true );
    consumer.getResourcesFromMeta( meta );
    Mockito.verify( meta, Mockito.times( 1 ) ).getParentStepMeta();
    Mockito.verify( (BaseFileMeta) meta, Mockito.times( 1 ) ).getFilePaths( false );
  }

  @Test
  public void testGetResourcesFromRow() throws Exception {
    Collection<IExternalResourceConsumer> resources = consumer.getResourcesFromRow( null, null, null );
    assertNotNull( resources );
    assertTrue( resources.isEmpty() );
  }
}

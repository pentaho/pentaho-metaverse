/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metaverse.api.MetaverseException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class TransExtensionPointUtilTest {

  @Mock
  TransMeta transMeta;

  @Before
  public void setUp() {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
    when( transMeta.getFilename() ).thenReturn( "/path/to/file.ktr" );
    when( transMeta.getName() ).thenReturn( "testTrans" );
  }

  @Test
  public void testDefaultConstructor() {
    assertNotNull( new TransExtensionPointUtil() );
  }

  @Test( expected = MetaverseException.class )
  public void testAddLineageGraphNullTransMeta() throws Exception {
    TransExtensionPointUtil.addLineageGraph( null );
  }

  @Test( expected = MetaverseException.class )
  public void testAddLineageGraphNullFilename() throws Exception {
    when( transMeta.getFilename() ).thenReturn( null );
    TransExtensionPointUtil.addLineageGraph( transMeta );
  }
}

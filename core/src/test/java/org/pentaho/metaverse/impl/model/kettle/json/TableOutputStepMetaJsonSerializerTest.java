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

package org.pentaho.metaverse.impl.model.kettle.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;

import static org.mockito.Mockito.spy;

/**
 * User: RFellows Date: 12/1/14
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class TableOutputStepMetaJsonSerializerTest {

  TableOutputStepMetaJsonSerializer serializer;
  LineageRepository repo;
  @Mock JsonGenerator json;
  @Mock SerializerProvider provider;
  TableOutputMeta meta;

  @BeforeClass
  public static void init() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    repo = new LineageRepository();
    serializer = new TableOutputStepMetaJsonSerializer( TableOutputMeta.class, repo );
    meta = spy( new TableOutputMeta() );
  }

  @Test
  public void testConstructor() throws Exception {
    serializer = new TableOutputStepMetaJsonSerializer( TableOutputMeta.class );
  }

  @Test
  public void testWriteFieldTransforms() throws Exception {
    serializer.writeFieldTransforms( meta, json, provider );
  }

  @Test
  public void testWriteCustomProperties() throws Exception {
    serializer.writeCustomProperties( meta, json, provider );
  }
}

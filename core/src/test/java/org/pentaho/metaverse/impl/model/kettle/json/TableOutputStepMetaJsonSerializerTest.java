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

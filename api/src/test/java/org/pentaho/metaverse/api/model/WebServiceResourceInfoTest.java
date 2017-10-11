/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.model;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by rfellows on 5/11/15.
 */
public class WebServiceResourceInfoTest {

  WebServiceResourceInfo resourceInfo;

  @Before
  public void setUp() throws Exception {
    resourceInfo = new WebServiceResourceInfo();
  }

  @Test
  public void testSetMethod() throws Exception {
    resourceInfo.setMethod( "myMethod" );
    assertEquals( resourceInfo.getAttributes().get( "method" ), "myMethod" );
  }

  @Test
  public void testSetBody() throws Exception {
    resourceInfo.setBody( "myBody" );
    assertEquals( resourceInfo.getAttributes().get( "body" ), "myBody" );
  }

  @Test
  public void testSetApplicationType() throws Exception {
    resourceInfo.setApplicationType( "JSON" );
    assertEquals( resourceInfo.getAttributes().get( "applicationType" ), "JSON" );
  }

  @Test
  public void testAddParameter() throws Exception {
    resourceInfo.addParameter( "param1", "value1" );
    Map parameters = (Map) resourceInfo.getAttributes().get( "parameters" );
    assertNotNull( parameters );
    assertEquals( parameters.get( "param1" ), "value1" );

    resourceInfo.addParameter( "param2", "value2" );
    parameters = (Map) resourceInfo.getAttributes().get( "parameters" );
    assertNotNull( parameters );
    assertEquals( parameters.get( "param2" ), "value2" );
  }

  @Test
  public void testAddHeader() throws Exception {
    resourceInfo.addHeader( "header1", "value1" );
    Map headers = (Map) resourceInfo.getAttributes().get( "headers" );
    assertNotNull( headers );
    assertEquals( headers.get( "header1" ), "value1" );

    resourceInfo.addHeader( "header2", "value2" );
    headers = (Map) resourceInfo.getAttributes().get( "headers" );
    assertNotNull( headers );
    assertEquals( headers.get( "header2" ), "value2" );
  }
}

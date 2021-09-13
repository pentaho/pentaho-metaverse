/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.graph;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.metaverse.graph.catalog.LineageDataResource;

public class LineageDataResourceTest {

  @Test
  public void testHdfsPathParser() {
    LineageDataResource dataResource = new LineageDataResource( "foo" );
    dataResource.parseHdfsPath( "/devuser:***@hdp31n1.pentaho.net:8020/user/prinehart/waterline/sales_data.csv" );
    Assert.assertEquals( "hdp31n1.pentaho.net", dataResource.getHdfsHost() );
    Assert.assertEquals( "8020", dataResource.getHdfsPort() );
    Assert.assertEquals( "/user/prinehart/waterline/sales_data.csv", dataResource.getPath() );
  }
}

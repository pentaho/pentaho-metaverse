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

package org.pentaho.metaverse.api.model.catalog;

import org.junit.Assert;
import org.junit.Test;

public class LineageDataResourceTest {

  @Test
  public void testHdfsPathParser() {
    LineageDataResource dataResource = new LineageDataResource( "foo" );
    dataResource.parseHdfsPath( "/devuser:***@hdp31n1.pentaho.net:8020/user/devuser/waterline/sales_data.csv" );
    Assert.assertEquals( "hdp31n1.pentaho.net", dataResource.getHdfsHost() );
    Assert.assertEquals( "8020", dataResource.getHdfsPort() );
    Assert.assertEquals( "/user/devuser/waterline/sales_data.csv", dataResource.getPath() );
  }

  @Test
  public void testS3PathParser() {
    LineageDataResource dataResource = new LineageDataResource( "foo" );
    dataResource.parseS3PvfsPath( "/bucketname/key/to/file.txt" );
    Assert.assertEquals( "bucketname", dataResource.getS3Bucket() );
    Assert.assertEquals( "key/to/file.txt", dataResource.getPath() );
  }
}

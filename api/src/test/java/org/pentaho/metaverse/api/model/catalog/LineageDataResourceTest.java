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


package org.pentaho.metaverse.api.model.catalog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LineageDataResourceTest {

  @Test
  public void testHdfsPathParser() {
    LineageDataResource dataResource = new LineageDataResource( "foo" );
    dataResource.parseHdfsPath( "/devuser:***@hdp31n1.pentaho.net:8020/user/devuser/waterline/sales_data.csv" );
    assertEquals( "hdp31n1.pentaho.net", dataResource.getHdfsHost() );
    assertEquals( "8020", dataResource.getHdfsPort() );
    assertEquals( "/user/devuser/waterline/sales_data.csv", dataResource.getPath() );
  }

  @Test
  public void testS3PathParser() {
    LineageDataResource dataResource = new LineageDataResource( "foo" );
    dataResource.parseS3PvfsPath( "/bucketname/key/to/file.txt" );
    assertEquals( "bucketname", dataResource.getS3Bucket() );
    assertEquals( "key/to/file.txt", dataResource.getPath() );
  }
}

package org.pentaho.metaverse.graph;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.metaverse.graph.catalog.LineageDataResource;

public class GraphCatalogWriterTest {

  @Test
  public void testHdfsPathParser() {
    GraphCatalogWriter graphCatalogWriter = new GraphCatalogWriter( "","","","","","" );
    LineageDataResource dataResource = new LineageDataResource( "foo" );
    graphCatalogWriter.parseHdfsHost( "/devuser:***@hdp31n1.pentaho.net:8020/user/prinehart/waterline/sales_data.csv", dataResource );
    Assert.assertEquals( "hdp31n1.pentaho.net", dataResource.getHdfsHost() );
    Assert.assertEquals( "8020", dataResource.getHdfsPort() );
  }
}

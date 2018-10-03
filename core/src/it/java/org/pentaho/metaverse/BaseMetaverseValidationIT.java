/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.api.IMetaverseReader;
import org.pentaho.metaverse.frames.RootNode;
import org.pentaho.metaverse.locator.FileSystemLocator;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;

public class BaseMetaverseValidationIT {

  protected static IMetaverseReader reader;
  protected static Graph graph;
  protected static FramedGraphFactory framedGraphFactory;
  protected static FramedGraph framedGraph;
  protected static RootNode root;

  public static final String REPO_ID = "FILE_SYSTEM_REPO"; // same as within pentahoObjects.spring.xml

  /**
   * Call in the child class's BeforeClass method.
   */
  public static void init( final String rootTestFolder, final String targetOutputFile ) throws Exception {
    IntegrationTestUtil.initializePentahoSystem( "src/it/resources/solution/system/pentahoObjects.spring.xml" );

    // we only care about the demo folder
    FileSystemLocator fileSystemLocator = PentahoSystem.get( FileSystemLocator.class );
    IDocumentLocatorProvider provider = PentahoSystem.get( IDocumentLocatorProvider.class );
    // remove the original locator so we can set the modified one back on it
    provider.removeDocumentLocator( fileSystemLocator );
    fileSystemLocator.setRootFolder( rootTestFolder );
    provider.addDocumentLocator( fileSystemLocator );

    MetaverseUtil.setDocumentController( PentahoSystem.get( IDocumentController.class ) );

    // build the graph using our updated locator/provider
    graph = IntegrationTestUtil.buildMetaverseGraph( provider );
    reader = PentahoSystem.get( IMetaverseReader.class );

    framedGraphFactory = new FramedGraphFactory( new GremlinGroovyModule() );
    framedGraph = framedGraphFactory.create( graph );
    root = (RootNode) framedGraph.getVertex( "entity", RootNode.class );

    File exportFile = new File( targetOutputFile );
    FileUtils.writeStringToFile( exportFile, reader.exportToXml(), "UTF-8" );
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    IntegrationTestUtil.shutdownPentahoSystem();
  }

  protected static String normalizeFilePath( final String tilePath ) {
    if ( StringUtils.isBlank( tilePath ) ) {
      return tilePath;
    }
    return tilePath.replace( "file://", "" ).replace( "/C:", "C:" ).replace( "\\", "/" );
  }

  protected static int getIterableSize( final Iterable<?> iterable ) {
    int count = 0;
    for ( Object o : iterable ) {
      if ( o != null ) {
        count++;
      }
    }
    return count;
  }
}

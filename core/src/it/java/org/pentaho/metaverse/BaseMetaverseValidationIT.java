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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.api.IMetaverseReader;
import org.pentaho.metaverse.frames.Concept;
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.frames.RootNode;
import org.pentaho.metaverse.frames.TransformationNode;
import org.pentaho.metaverse.frames.TransformationStepNode;
import org.pentaho.metaverse.locator.FileSystemLocator;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.pentaho.dictionary.DictionaryConst.LINK_CONTAINS;
import static org.pentaho.dictionary.DictionaryConst.LINK_TYPE_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.LINK_WRITESTO;

public abstract  class BaseMetaverseValidationIT {

  protected static IMetaverseReader reader;
  protected static Graph graph;
  protected static FramedGraphFactory framedGraphFactory;
  protected static FramedGraph framedGraph;
  protected static RootNode root;
  protected static Map<String, Concept> entityNodes = new HashMap();

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

    framedGraphFactory = new FramedGraphFactory( new GremlinGroovyModule(), new JavaHandlerModule() );
    framedGraph = framedGraphFactory.create( graph );
    root = (RootNode) framedGraph.getVertex( "entity", RootNode.class );
    final List<Vertex> allVertices = IteratorUtils.toList( framedGraph.getVertices().iterator() );
    for ( final Vertex vertex : allVertices ) {
      final String vertexId = vertex.getId().toString();
      if ( vertexId.startsWith( "entity_" ) ) {
        entityNodes.put( vertexId.replace( "entity_", "" ),
          (Concept) framedGraph.getVertex( vertexId, Concept.class ) );
      }
    }

    File exportFile = new File( targetOutputFile );
    FileUtils.writeStringToFile( exportFile, reader.exportToXml(), "UTF-8" );
  }

  @AfterClass
  public static void cleanUpClass() throws Exception {
    IntegrationTestUtil.shutdownPentahoSystem();
  }

  @After
  public void cleanUpInstance() throws Exception {
    if ( shouldCleanupInstance() ) {
      IntegrationTestUtil.shutdownPentahoSystem();
    }
  }

  /**
   * Returns true if the test cleaup should occur after every test instance (within the method marked with the @After
   * annotation) rather than just when the class is being torn down (within the method annotated with @AfterClass)
   */
  protected abstract boolean shouldCleanupInstance();

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

  /**
   * Verifies that the transformation node with the given name exists in the graph.
   *
   * @param transformationName the transformation node name
   * @param isSubTrans         true if looking for a node corresponding to a sub-transformation (executed by some step),
   *                           false otherwise
   * @return the verified {@link TransformationNode}
   */
  protected TransformationNode verifyTransformationNode( final String transformationName, final boolean isSubTrans ) {
    // verify the existence of two transformation nodes - one for the injector and one for the sub-transformation
    final List<TransformationNode> allTransformations = IteratorUtils.toList( root.getTransformations().iterator() );
    final TransformationNode node = isSubTrans ? root.getSubTransformation( transformationName )
      : root.getTransformation( transformationName );
    assertNotNull( node );
    assertTrue( allTransformations.contains( node ) );
    return node;
  }

  /**
   * Verifies that the {@link TransformationNode} contains the expected steps.
   *
   * @param transNode the {@link TransformationNode}
   * @param stepNames the expected non-virtual step names
   * @param isVirtual is true, we verify virtual steps, if false, we verify non-virtual steps
   * @return a {@link Map} of step name to stepNode, for convenient lookup by the caller.
   */
  protected Map<String, FramedMetaverseNode> verifyTransformationSteps(
    final TransformationNode transNode, final String[] stepNames, final boolean isVirtual ) {

    final List<FramedMetaverseNode> stepNodes = IteratorUtils.toList(
      ( isVirtual ? transNode.getVirtualStepNodes() : transNode.getStepNodes() ).iterator() );
    assertEquals( stepNames == null ? 0 : stepNames.length, stepNodes.size() );
    // check that all the expected step nodes exist and create a node map on the fly for future use
    final Map<String, FramedMetaverseNode> stepNodeMap = verifyNodeNames( stepNodes,
      DictionaryConst.NODE_TYPE_TRANS_STEP, stepNames );

    return stepNodeMap;
  }

  protected Map<String, FramedMetaverseNode> verifyNodes( final List<FramedMetaverseNode> nodes,
                                                          final TestLineageNode... expectedNodeArray ) {
    if ( expectedNodeArray == null ) {
      assertEquals( nodes.size(), 0 );
    }
    assertEquals( nodes.size(), expectedNodeArray.length );
    final List<TestLineageNode> expectedNodes = Arrays.asList(
      expectedNodeArray == null ? new TestLineageNode[] {} : expectedNodeArray );
    return verifyNodes( nodes, expectedNodes );
  }

  /**
   * Verifies that the {@code nodes} {@link List} contains nodes with names stored in the provided {@code
   * expectedNodeNameArray}
   *
   * @param nodes         a {@link List} of {@link FramedMetaverseNode}s
   * @param expectedNodes an {@link List} of {@link TestLineageNode}s
   * @return a {@link Map} of node name to node, for convenient lookup by the caller.
   */
  protected Map<String, FramedMetaverseNode> verifyNodes( final List<FramedMetaverseNode> nodes,
                                                          final List<TestLineageNode> expectedNodes ) {
    assertEquals( nodes.size(), expectedNodes.size() );
    final Map<String, FramedMetaverseNode> nodeMap = new HashMap();
    for ( final FramedMetaverseNode node : nodes ) {
      boolean matchFound = false;
      for ( final TestLineageNode expectedNode : expectedNodes ) {
        if ( expectedNode.getName().equals( node.getName() ) && expectedNode.getType().equals( node.getType() )
          && expectedNode.isVirtual() ==  node.isVirtual() ) {
            matchFound = true;
          break;
        }
      }
      assertTrue( String.format( "Unable to find match for %svirtual node '%s' of type '%s'",
        node.isVirtual() ? "" : "non-", node.getName(), node.getType() ), matchFound );
      nodeMap.put( node.getName(), node );
    }
    return nodeMap;
  }

  private Map<String, FramedMetaverseNode> verifyNodeNames( final List<FramedMetaverseNode> nodes,
                                                            final String nodeType,
                                                            final String... expectedNodeNameArray ) {
    final List<String> expectedNodeNames = Arrays.asList(
      expectedNodeNameArray == null ? new String[] {} : expectedNodeNameArray );
    return verifyNodeNames( nodes, nodeType, expectedNodeNames );
  }

  /**
   * Verifies that the {@code nodes} {@link List} contains nodes with names stored in the provided {@code
   * expectedNodeNameArray}
   *
   * @param nodes             a {@link List} of {@link FramedMetaverseNode}s
   * @param expectedNodeNames an {@link List} of node names
   * @return a {@link Map} of node name to node, for convenient lookup by the caller.
   */
  private Map<String, FramedMetaverseNode> verifyNodeNames( final List<FramedMetaverseNode> nodes,
                                                            final String nodeType,
                                                            final List<String> expectedNodeNames ) {
    final List<String> expectedNodeNameClone = new ArrayList<>( expectedNodeNames );
    assertEquals( String.format( "Incorrect number of '%s' nodes", nodeType ),
      expectedNodeNames.size() , nodes.size() ) ;
    final Map<String, FramedMetaverseNode> nodeMap = new HashMap();
    for ( final FramedMetaverseNode node : nodes ) {
      assertTrue( String.format( "Node '%s' is not of type '%s'", node.getName(), nodeType ),
        expectedNodeNameClone.remove( node.getName() ) );
      nodeMap.put( node.getName(), node );
    }
    return nodeMap;
  }

  protected void verifyNodesTypes( final Map<String, List<String>> nodeTypeMap ) {

    final List<Map.Entry<String, List<String>>> nodeTypeMapList = IteratorUtils.toList(
      nodeTypeMap.entrySet().iterator() );
    for ( final Map.Entry<String, List<String>> nodeTypeMapEntry : nodeTypeMapList ) {
      final String nodeType = nodeTypeMapEntry.getKey();
      // get the entity node corresponding to this type
      final Concept entityNode = entityNodes.get( nodeType );
      assertNotNull( String.format( "Entity node for type '%s' not found", nodeType), entityNode );
      final List<String> nodeNames = nodeTypeMapEntry.getValue();
      final List<FramedMetaverseNode> nodes = IteratorUtils.toList( entityNode.getConcreteNodes().iterator() );
      verifyNodeNames( nodes, nodeType, nodeNames );
    }
  }


  protected void verifyStepIOLinks( final TransformationStepNode stepNode, final TestLineageLink... links ) {
    final List<TestLineageLink> linkList = Arrays.asList( links == null ? new TestLineageLink[] {} : links );
    verifyStepIOLinks( stepNode, linkList );
  }

  /**
   * Verifies that the inputs and output of the given {@code stepNode} are linked correctly.
   *
   * @param stepNode a {@link TransformationStepNode}
   * @param links    a {@link List} of {@link TestLineageLink}s
   */
  protected void verifyStepIOLinks( final TransformationStepNode stepNode, final List<TestLineageLink> links ) {

    final List<FramedMetaverseNode> inputs = IteratorUtils.toList( stepNode.getInputStreamFields().iterator() );
    final List<FramedMetaverseNode> outputs = IteratorUtils.toList( stepNode.getOutputStreamFields().iterator() );

    for ( final TestLineageLink link : links ) {
      FramedMetaverseNode inputNode = findNode( inputs, link.getInputNode() );
      assertNotNull( inputNode );
      FramedMetaverseNode outptuNode = findNode( outputs, link.getOutputNode() );
      assertNotNull( outptuNode );
      final List<Concept> linkedNodes = IteratorUtils.toList(
        inputNode.getOutNodes( link.getLinkLabel() ).iterator() );
      assertTrue( linkedNodes.size() > 0 );
      // find a match
      boolean matchFound = false;
      for ( final Concept linkedNode : linkedNodes ) {
        if ( linkedNode.equals( outptuNode ) ) {
          matchFound = true;
          break;
        }
      }
      assertTrue( matchFound );
    }
  }

  public FramedMetaverseNode verifyLinkedNode( final FramedMetaverseNode fromNode, final String linkLabel,
                                               final String toNodeName ) {
    final List<FramedMetaverseNode> outNodes = IteratorUtils.toList( fromNode.getOutNodes( linkLabel ).iterator() );
    FramedMetaverseNode searchNode = null;
    for ( final FramedMetaverseNode outNode : outNodes ) {
      if ( outNode.getName().equals( toNodeName ) ) {
        searchNode = outNode;
        break;
      }
    }
    assertNotNull( searchNode );
    return searchNode;

  }

  public static final String SKIP = "SKIP_COMPARISON";

  public void verifyNodeProperties( final FramedMetaverseNode node, final Map<String, Object> expectedProperties ) {
    final Iterator<String> propertyNames = node.getPropertyNames().iterator();
    final Set<String> encounteredProperties = new HashSet();
    final Set<String> badProperties = new HashSet();
    while ( propertyNames.hasNext() ) {
      final String propertyName = propertyNames.next();
      final Object expectedValue = expectedProperties.get( propertyName );
      final Object actualValue = node.getProperty( propertyName );
      if ( actualValue == null ) {
        badProperties.add( propertyName );
        continue;
      }
      if ( !SKIP.equals( expectedValue ) ) {
        assertEquals( String.format( "Incorrect value for property '%s'", propertyName ),
          toSafeStringSafely( expectedValue ), toSafeStringSafely( actualValue ) );
      }
      encounteredProperties.add( propertyName );
    }
    final Set<String> missingProperties = new HashSet( node.getPropertyNames() );
    missingProperties.removeAll( encounteredProperties );
    assertEquals( String.format( "Missing expected properties: %s", String.join( ",", missingProperties ) ), 0,
      missingProperties.size() );
  }

  private String toSafeStringSafely( final Object originalString) {
    return originalString == null ? "" : originalString.toString();
  }

  protected FramedMetaverseNode findNode( final List<FramedMetaverseNode> nodes, final TestLineageNode node ) {
    return findNode( nodes, node.getName(), node.getType() );
  }

  protected FramedMetaverseNode findNode( final List<FramedMetaverseNode> nodes, final String nodeName,
                                          final String nodeType ) {
    for ( final FramedMetaverseNode node : nodes ) {
      if ( node.getName().equals( nodeName ) && node.getType().equals( nodeType ) ) {
        return node;
      }
    }
    return null;
  }


  ///// ----------------- Helper wrapper node objects and methods


  protected TestTransformationNode testTransformationNode( final String name, final boolean virtual ) {
    return new TestTransformationNode( name, virtual );
  }

  protected TestSqlQueryNode testSqlQueryNode( final String name, final boolean virtual ) {
    return new TestSqlQueryNode( name, virtual );
  }

  protected TestTableNode testTableNode( final String name, final boolean virtual ) {
    return new TestTableNode( name, virtual );
  }

  protected TestStepNode testStepNode( final String name, final boolean virtual ) {
    return new TestStepNode( name, virtual );
  }

  protected TestFieldNode testFieldNode( final String name, final boolean virtual ) {
    return new TestFieldNode( name, virtual );
  }

  protected TestColumnNode testColumnNode( final String name, final boolean virtual ) {
    return new TestColumnNode( name, virtual );
  }

  protected TestLineageLink testLineageLink(  final TestLineageNode inputNode, final String linkLabel,
                                           final TestLineageNode outputNode ) {
    return new  TestLineageLink( inputNode, linkLabel, outputNode );
  }

  protected TestLineageNode testLineageNode( final FramedMetaverseNode node ) {
    return new TestLineageNode( node.getType(), node.getName(), node.isVirtual() );
  }

  protected class TestTransformationNode extends TestLineageNode {

    public TestTransformationNode( final String name, final boolean virtual ) {
      super( DictionaryConst.NODE_TYPE_TRANS_STEP, name, virtual );
    }
  }

  protected class TestSqlQueryNode extends TestLineageNode {

    public TestSqlQueryNode( final String nam, final boolean virtual ) {
      super( DictionaryConst.NODE_TYPE_SQL_QUERY, nam, virtual );
    }
  }

  protected class TestTableNode extends TestLineageNode {

    public TestTableNode( final String name, final boolean virtual ) {
      super( DictionaryConst.NODE_TYPE_DATA_TABLE, name, virtual );
    }
  }

  protected class TestStepNode extends TestLineageNode {

    public TestStepNode( final String name, final boolean virtual ) {
      super( DictionaryConst.NODE_TYPE_TRANS_STEP, name, virtual );
    }
  }

  protected class TestFieldNode extends TestLineageNode {

    public TestFieldNode( final String name, final boolean virtual ) {
      super( DictionaryConst.NODE_TYPE_TRANS_FIELD, name, virtual );
    }
  }

  protected class TestColumnNode extends TestLineageNode {

    public TestColumnNode( final String name, final boolean virtual ) {
      super( DictionaryConst.NODE_TYPE_DATA_COLUMN, name, virtual );
    }
  }

  protected class TestLineageNode {

    private String type;
    private String name;
    private boolean virtual;

    public TestLineageNode( final String type, final String name, final boolean virtual ) {
      this.type = type;
      this.name = name;
      this.virtual = virtual;
    }

    public String getType() {
      return type;
    }

    public void setType( String type ) {
      this.type = type;
    }

    public String getName() {
      return name;
    }

    public void setName( String name ) {
      this.name = name;
    }

    public boolean isVirtual() {
      return virtual;
    }
  }

  protected class TestLineageLink {

    private TestLineageNode inputNode;
    private String linkLabel;
    private TestLineageNode outputNode;
    public TestLineageLink( final TestLineageNode inputNode, final String linkLabel,
                            final TestLineageNode outputNode ) {
      this.inputNode = inputNode;
      this.linkLabel = linkLabel;
      this.outputNode = outputNode;
    }

    public TestLineageNode getInputNode() {
      return inputNode;
    }

    public String getLinkLabel() {
      return linkLabel;
    }

    public TestLineageNode getOutputNode() {
      return outputNode;
    }

  }
}

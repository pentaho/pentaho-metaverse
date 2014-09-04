/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.metaverse;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.TransformationRuntimeExtensionPoint;
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.graph.GraphMLWriter;
import com.pentaho.metaverse.locator.FileSystemLocator;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Set;

/**
 * User: RFellows Date: 8/28/14
 */
public class StaticAndRuntimeGraphMergeIT {

  private static final String RUNTIME_LOCATOR_TOKEN = "EXTENSION-POINT-LOCATOR";
  private static final String STATIC_LOCATOR_VALUE = "FileSystem~FILE_SYSTEM_REPO";
  private static IMetaverseReader reader;
  private static Graph staticGraph;
  private static Graph runtimeGraph;

  private static final String REPO_PATH = "src/it/resources/repo/runtime";
  private static final String KTR = REPO_PATH + "/Textfile input - filename from field.ktr";//"/file_to_table.ktr";

  private static File runtimeGraphmlFile;

  private String runtimeId;

  private TransMeta tm;

  @BeforeClass
  public static void init() throws Exception {
    // define the static graph/set one up.
    IntegrationTestUtil.initializePentahoSystem( "src/it/resources/solution" );

    FileSystemLocator dl = PentahoSystem.get( FileSystemLocator.class );
    dl.setRootFolder( "src/it/resources/repo/demo" );

    reader = PentahoSystem.get( IMetaverseReader.class );
    staticGraph = IntegrationTestUtil.buildMetaverseGraph();

    ExtensionPointPluginType.getInstance().registerCustom( TransformationRuntimeExtensionPoint.class, "custom",
        "transRuntimeMetaverse", "TransformationStartThreads", "no description", null );

    PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );

    KettleEnvironment.init();

    List<PluginInterface> plugins = PluginRegistry.getInstance().getPlugins( ExtensionPointPluginType.class );
    System.out.println( plugins );
  }

  @Before
  public void setup() throws Exception {

    FileInputStream xmlStream = new FileInputStream( KTR );
    Variables vars = new Variables();
    vars.setVariable( "testTransParam3", "demo" );
    // run the trans
    tm = new TransMeta( xmlStream, null, true, vars, null );
    tm.setFilename( tm.getName() );
    Trans trans = new Trans( tm, null, tm.getName(), REPO_PATH, KTR );
    trans.setVariable( "testTransParam3", "demo" );
    trans.setVariable( "Internal.Transformation.Filename.Directory", REPO_PATH );
    tm.setVariable( "testTransParam3", "demo" );
    trans.execute( null );
    trans.waitUntilFinished();

    runtimeId = trans.getLogChannelId();

    // get the runtime graph
    runtimeGraphmlFile = new File( tm.getFilename() + " - export.graphml" );
    FileInputStream fis = new FileInputStream( runtimeGraphmlFile );

    runtimeGraph = new TinkerGraph();
    GraphMLReader graphMLReader = new GraphMLReader( runtimeGraph );
    graphMLReader.inputGraph( fis );

  }

  @Test
  public void testMergeGraphs() throws Exception {

    Vertex runtimeNode = addRuntimeNode();

    // add in any new vertices not in the static graph
    Iterable<Vertex> runtimeVertices = runtimeGraph.getVertices();
    for ( Vertex rv : runtimeVertices ) {
      // look it up in the static graph
      String lookupId = resolveId( rv );
      Vertex sv = staticGraph.getVertex( lookupId );

      if ( sv == null ) {
        System.out.println( "Adding runtime vertex - " + rv.toString() );
        Vertex added = cloneVertexWithNewId( rv, lookupId );

        // is this possibly a variableized id?
        String undoVars = undoVarsForId( lookupId );
        if ( undoVars != lookupId ) {
          Vertex varVertex = staticGraph.getVertex( undoVars );
          if ( varVertex != null ) {
            // add a link
            Edge e = staticGraph.addEdge( null, added, varVertex, "resolvesto" );
            e.setProperty( "text", "resolvesto" );
          }
        }

        // link it to the runtime node if it's not already in the graph?
        if ( !added.getProperty( DictionaryConst.PROPERTY_TYPE )
            .equals( DictionaryConst.NODE_TYPE_EXECUTION_ENGINE ) ) {

          Edge edge = staticGraph.addEdge( null, runtimeNode, added, "defines" );
          edge.setProperty( "text", "defines" );
        }

      } else {
        // node was in the static graph already
        // do we need to update anything???
      }
    }

    // add in any edges that aren't in the static graph
    Iterable<Edge> runtimeEdges = runtimeGraph.getEdges();
    for ( Edge re : runtimeEdges ) {
      String lookupId = re.getId().toString().replace( RUNTIME_LOCATOR_TOKEN, STATIC_LOCATOR_VALUE );
      Edge se = staticGraph.getEdge( lookupId );

      if ( se == null ) {
        System.out.println( "Adding runtime edge - " + re.toString() );
        // both vertices should already be in the graph, go get them
        Vertex outV = staticGraph.getVertex( resolveId( re.getVertex( Direction.OUT ) ) );
        Vertex inV = staticGraph.getVertex( resolveId( re.getVertex( Direction.IN ) ) );

        Edge added = staticGraph.addEdge( lookupId, outV, inV, re.getLabel() );
        added.setProperty( "text", re.getLabel() );
      } else {
        // edge already in the graph, don't need to add it
      }
    }

    // output the merged graph
    FileOutputStream fos = new FileOutputStream( "src/it/resources/mergedGraph.graphml" );
    GraphMLWriter writer = new GraphMLWriter();
    writer.outputGraph( staticGraph, fos );
  }

  private Vertex addRuntimeNode() {
    // add a runtime node so we can identify them
    Vertex node = runtimeGraph.getVertex( runtimeId );
    Vertex runtimeNode = cloneVertexWithNewId( node, node.getId().toString() );
    return runtimeNode;
  }

  private String undoVarsForId( String lookupId ) {

    // this is a hack, not intended to be used post-poc. we need to get the real value
    // before the variables are substituted in somehow and keep that info with the runtime graph

    List<String> usedVariables = tm.getUsedVariables();
    for ( String usedVariable : usedVariables ) {
      String value = tm.getVariable( usedVariable );
      if ( value != null && value.trim().length() > 0 ) {
        // try to put the variable name in rather than the value
        lookupId = lookupId.replace( "~" + value + "~", "~${" + usedVariable + "}~" );
      }
    }
    return lookupId;
  }

  private Vertex cloneVertexWithNewId( Vertex vertexToClone, String newId ) {
    Vertex v = staticGraph.addVertex( newId );
    Set<String> propertyKeys = vertexToClone.getPropertyKeys();
    for ( String propertyKey : propertyKeys ) {
      v.setProperty( propertyKey, vertexToClone.getProperty( propertyKey ) );
    }
    return v;
  }

  private String resolveId( Vertex runtimeVertex ) {
    return resolveId( runtimeVertex.getId().toString() );
  }

  private String resolveId( String runtimeId ) {
    return runtimeId.replace( RUNTIME_LOCATOR_TOKEN, STATIC_LOCATOR_VALUE );
  }

}

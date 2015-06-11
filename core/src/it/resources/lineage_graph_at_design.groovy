import org.pentaho.di.core.extension.ExtensionPointPluginType

metaversePlugin = plugins.findPluginWithId(ExtensionPointPluginType, 'transChangeLineageGraph')

// The lineage graph map (and all metaverse stuff) is in different plugin(s) than the GroovyConsole
cl = plugins.getClassLoader(metaversePlugin)
Thread.currentThread().setContextClassLoader(cl)
LineageGraphMap = cl.loadClass('LineageGraphMap')
Direction = cl.loadClass('com.tinkerpop.blueprints.Direction')
GraphMLWriter = cl.loadClass('GraphMLWriter')

transAnalysis = LineageGraphMap.getInstance().get( activeTrans );
try {
  graph = transAnalysis.get();
  graph.vertices.each { v ->
    println( "Found node: ${v.getProperty('name')} (${v.getProperty('type') ?: ''}${v.getProperty('virtual') ? 'VIRTUAL' : ''})" );
  }
  println
  graph.edges.each { e ->
    inV = e.getVertex(Direction.IN).getProperty('name')
    outV = e.getVertex(Direction.OUT)?.getProperty('name')
    println( "Found edge: $outV -[ $e.label ]-> $inV" );
  }
} catch ( Exception e ) {
  println( "error during analysis", e );
}
//GraphMLWriter.newInstance().outputGraph(graph, new FileOutputStream('/Users/mburgess/xpt_test.graphml') )
true
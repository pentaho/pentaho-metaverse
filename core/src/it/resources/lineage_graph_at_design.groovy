import org.pentaho.di.core.extension.ExtensionPointPluginType

metaversePlugin = plugins.findPluginWithId(ExtensionPointPluginType, 'transChangeLineageGraph')

// The lineage graph map (and all metaverse stuff) is in different plugin(s) than the GroovyConsole
cl = plugins.getClassLoader(metaversePlugin)
Thread.currentThread().setContextClassLoader(cl)
LineageGraphMap = cl.loadClass('LineageGraphMap')
GraphMLWriter = cl.loadClass('GraphMLWriter')

transAnalysis = LineageGraphMap.getInstance().get( activeTrans );
try {
  graph = transAnalysis.get();
  graph.vertices().each { v ->
    name = v.property('name').isPresent() ? v.value('name') : ''
    type = v.property('type').isPresent() ? v.value('type') : ''
    virtual = v.property('virtual').isPresent() && v.value('virtual')
    println( "Found node: ${name} (${type}${virtual ? 'VIRTUAL' : ''})" );
  }
  println
  graph.edges().each { e ->
    inV = e.inVertex().property('name').isPresent() ? e.inVertex().value('name') : ''
    outV = e.outVertex().property('name').isPresent() ? e.outVertex().value('name') : ''
    println( "Found edge: $outV -[ ${e.label()} ]-> $inV" );
  }
} catch ( Exception e ) {
  println( "error during analysis", e );
}
//GraphMLWriter.newInstance().outputGraph(graph, new FileOutputStream('/Users/mburgess/xpt_test.graphml') )
true
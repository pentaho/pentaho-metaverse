package com.pentaho.metaverse.graph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * A concrete sub-class of BaseGraphMetaverseReader that uses in-memory TinkerGraph as the graph
 * @author jdixon
 *
 */
public class TinkerGraphMetaverseReader extends BaseGraphMetaverseReader {

  /**
   * A path to a graphml file to use as the initial state of the graph.
   * If this is null a fresh, empty graph is created in memory.
   */
  public static String filePath;

  private static final long serialVersionUID = -68131573133600167L;

  private static final Log LOGGER = LogFactory.getLog( TinkerGraphMetaverseReader.class );

  private TinkerGraph graph;

  /**
   * Constructor that loads the initial state from an external file (if specified)
   * @throws Exception If a problem is encountered
   */
  public TinkerGraphMetaverseReader() throws Exception {
    /* is this useful code?
    if ( filePath != null ) {
      if ( filePath.endsWith( ".dat" ) ) {
        graph = new TinkerGraph( filePath );
      } else if ( filePath.endsWith( ".graphml" ) ) {
        graph = new TinkerGraph();
        GraphMLReader reader = new GraphMLReader( graph );
        reader.inputGraph( filePath );
      }
    } else {
    }
    */
    graph = new TinkerGraph();
  }

  @Override
  protected Graph getGraph() {
    return graph;
  }

  @Override
  public Log getLogger() {
    return LOGGER;
  }

}

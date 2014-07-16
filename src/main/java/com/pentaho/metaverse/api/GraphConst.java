package com.pentaho.metaverse.api;

import java.util.HashSet;
import java.util.Set;

/**
 * Constants used in the graph building and reading.
 * All data flow links must be left to right (source to destination).
 * All structural links must be high to low (container to containee). 
 * @author jdixon
 *
 */
public class GraphConst {

  /**
   * Label for an "executes" edge in the graph, e.g. a job executes a transformation
   */
  public static final String LINK_EXECUTES = "executes";

  /**
   * Label for an "contains" edge in the graph, e.g. a transformation contains a step
   */
  public static final String LINK_CONTAINS = "contains";

  /**
   * Label for an "populates" edge in the graph, e.g. a transformation field populates a table column.
   */
  public static final String LINK_POPULATES = "populates";

  /**
   * Label for an "is ready by" edge in the graph, e.g. a text file is ready by a transformation step
   */
  public static final String LINK_READBY = "isreadby";

  /**
   * Label for an "writes to" edge in the graph, e.g. a table output step writes to a table
   */
  public static final String LINK_WRITESTO = "writesto";

  /**
   * Label for an "creates" edge in the graph, e.g. a transformation step creates a transformation field
   */
  public static final String LINK_CREATES = "creates";

  /**
   * The set of structural link types
   */
  public static final Set<String> STRUCTURAL_LINK_MAP = new HashSet<String>();

  /**
   * The set of data flow link types
   */
  public static final Set<String> DATAFLOW_LINK_MAP = new HashSet<String>();

  static {
    STRUCTURAL_LINK_MAP.add( LINK_EXECUTES );
    STRUCTURAL_LINK_MAP.add( LINK_CONTAINS );
    STRUCTURAL_LINK_MAP.add( LINK_CREATES );
    DATAFLOW_LINK_MAP.add( LINK_POPULATES );
    DATAFLOW_LINK_MAP.add( LINK_READBY );
    DATAFLOW_LINK_MAP.add( LINK_WRITESTO );
  }

}

package com.pentaho.dictionary;

/**
 * Constants used in the graph building and reading.
 * All data flow links must be left to right (source to destination).
 * All structural links must be high to low (container to containee). 
 *
 * @author jdixon
 */
public class DictionaryConst {

  /**
   * Property key for "type". Value could be NODE_TYPE_TRANS or NODE_TYPE_CONTENT etc
   */
  public static final String PROPERTY_TYPE = "type";

  /**
   * Localized type
   */
  public static final String PROPERTY_TYPE_LOCALIZED = "typeLocale";

  /**
   * Property key for "category". If the type is NODE_TYPE_TRANS, the category could be
   * "Document" etc
   */
  public static final String PROPERTY_CATEGORY = "category";

  /**
   * Localized category
   */
  public static final String PROPERTY_CATEGORY_LOCALIZED = "categoryLocale";

  /**
   * Property key for "id"
   */
  public static final String PROPERTY_ID = "id";

  /**
   * Property key for "name"
   */
  public static final String PROPERTY_NAME = "name";

  /**
   * Property key for "author"
   */
  public static final String PROPERTY_AUTHOR = "author";

  /**
   * Property key for suggested "color" for a node when a graph is visualized
   */
  public static final String PROPERTY_COLOR = "color";

  /**
   * Property key for "last modified"
   */
  public static final String PROPERTY_LAST_MODIFED = "lastmodified";

  /**
   * Label for an "executes" edge in the graph, e.g. a job executes a transformation
   */
  public static final String LINK_EXECUTES = "executes";

  /**
   * Label for an "parent concept" edge in the graph, e.g. a transformation entity is the parent concept of a ktr
   */
  public static final String LINK_PARENT_CONCEPT = "parentconcept";

  /**
   * Label for an "contains" edge in the graph, e.g. a transformation contains a step
   */
  public static final String LINK_CONTAINS = "contains";

  /**
   * Label for an "defines" edge in the graph, e.g. a transformation contains a step
   */
  public static final String LINK_DEFINES = "defines";

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
   * Label for a "dependency of" edge in the graph, e.g. a databaseMeta is a dependency of a step
   */
  public static final String LINK_DEPENDENCYOF = "dependencyof";

  /**
   * Label for an "creates" edge in the graph, e.g. a transformation step creates a transformation field
   */
  public static final String LINK_CREATES = "creates";

  /**
   * The node type for document locator objects
   */
  public static final String NODE_TYPE_LOCATOR = "Locator";
  // TODO temporary; need to get the demo working; permanent fix in progress

  /**
   * The node type for PDI jobs
   */
  public static final String NODE_TYPE_JOB = "Job";

  /**
   * The node type for PDI transformations
   */
  public static final String NODE_TYPE_TRANS = "Transformation";

  /**
   * The node type for PDI transformation steps
   */
  public static final String NODE_TYPE_TRANS_STEP = "Transformation Step";

  /**
   * The node type for PDI job entries
   */
  public static final String NODE_TYPE_JOB_ENTRY = "Job Entry";

  /**
   * The node type for data sources, e.g. a database
   */
  public static final String NODE_TYPE_DATASOURCE = "Database Connection";

  /**
   * The node type for data tables
   */
  public static final String NODE_TYPE_DATA_TABLE = "Database Table";

  /**
   * The node type for PDI transformation fields
   */
  public static final String NODE_TYPE_TRANS_FIELD = "Transformation Stream field";

  /**
   * The node type for a data column in a data table
   */
  public static final String NODE_TYPE_DATA_COLUMN = "Database Column";

  /**
   * The node type for a physical file
   */
  public static final String NODE_TYPE_FILE = "File";

  /**
   * The node type for a field in a physical file
   */
  public static final String NODE_TYPE_FILE_FIELD = "File Field";

  /**
   * The node type for a web service, e.g. SalesForce.com or a HTTP call
   */
  public static final String NODE_TYPE_WEBSERVICE = "Web Service";

  /**
   * The node type for a logical model, e.g. a Mondrian schema
   */
  public static final String NODE_TYPE_LOGICAL_MODEL = "Logical Model";

  /**
   * The node type for a logical hierarchy, e.g. a Mondrian hierarchy
   */
  public static final String NODE_TYPE_LOGICAL_HIERARCHY = "Logical Hierarchy";

  /**
   * The node type for a field in a model, e.g. a Mondrian level
   */
  public static final String NODE_TYPE_LOGICAL_FIELD = "Model Field";

  /**
   * The entity node type for user content, e.g. transformations and jobs
   */
  public static final String NODE_TYPE_USER_CONTENT = "User Content";

  /**
   * The entity node type
   */
  public static final String NODE_TYPE_ENTITY = "Entity";

  /**
   * The root node type
   */
  public static final String NODE_TYPE_ROOT_ENTITY = "Root_Entity";

  /**
   * The entity node type for virtual nodes
   */
  public static final String NODE_VIRTUAL = "virtual";

  /**
   * The category for abstract nodes, e.g. entity nodes
   */
  public static final String CATEGORY_ABSTRACT = "abstract";

  /**
   * The category for abstract nodes, e.g. entity nodes
   */
  public static final String CATEGORY_DATASOURCE = "datasource";

  /**
   * The category for document nodes, e.g. transformations and jobs
   */
  public static final String CATEGORY_DOCUMENT = "document";

  /**
   * The category for document elements, e.g. transformation steps within a transformation
   */
  public static final String CATEGORY_DOCUMENT_ELEMENT = "documentelement";

  /**
   * The category for fields, e.g. transformation fields or table columns
   */
  public static final String CATEGORY_FIELD = "field";

  /**
   * The category for field collections, e.g. physical files, database tables
   */
  public static final String CATEGORY_FIELD_COLLECTION = "collection";

  /**
   * The category for repositories
   */
  public static final String CATEGORY_REPOSITORY = "repository";

  /**
   * The category for other node types
   */
  public static final String CATEGORY_OTHER = "other";

  /**
   * The suggested color for abstract nodes
   */
  public static final String COLOR_ABSTRACT = "#dddddd";

  /**
   * The suggested color for datasource nodes
   */
  public static final String COLOR_DATASOURCE = "#ff6600";

  /**
   * The suggested color for abstract nodes
   */
  public static final String COLOR_DOCUMENT = "#ccffcc";

  /**
   * The suggested color for document nodes
   */
  public static final String COLOR_DOCUMENT_ELEMENT = "#ccffff";

  /**
   * The suggested color for field nodes
   */
  public static final String COLOR_FIELD = "#ffcc99";

  /**
   * The suggested color for field collection nodes
   */
  public static final String COLOR_FIELD_COLLECTION = "#ff9900";

  /**
   * The suggested color for repositories
   */
  public static final String COLOR_REPOSITORY = "#66aa44";

  /**
   * The suggested color for nodes of unknown or "other" types
   */
  public static final String COLOR_OTHER = "#ffcccc";


  /**
   * Hides the constructor so that this class cannot be instanced
   */
  protected DictionaryConst() {
    throw new UnsupportedOperationException();
  }

}

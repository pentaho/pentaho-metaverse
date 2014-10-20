package com.pentaho.dictionary;

import com.pentaho.metaverse.impl.MetaverseLogicalIdGenerator;
import org.pentaho.platform.api.metaverse.ILogicalIdGenerator;

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
   * Property key for the path to a document, instance, etc.
   */
  public static final String PROPERTY_PATH = "path";

  /**
   * Property key for suggested "color" for a node when a graph is visualized
   */
  public static final String PROPERTY_COLOR = "color";

  /**
   * Property key for "last modified"
   */
  public static final String PROPERTY_LAST_MODIFIED = "lastmodified";

  /**
   * Property key for "last modified by"
   */
  public static final String PROPERTY_LAST_MODIFIED_BY = "lastmodifiedby";

  /**
   * Property key for "created"
   */
  public static final String PROPERTY_CREATED = "created";

  /**
   * Property key for "created by"
   */
  public static final String PROPERTY_CREATED_BY = "createdby";

  /**
   * Property key for "description"
   */
  public static final String PROPERTY_DESCRIPTION = "description";

  /**
   * Property key for "artifact version"
   */
  public static final String PROPERTY_ARTIFACT_VERSION = "version";

  /**
   * Property key for "status"
   */
  public static final String PROPERTY_STATUS = "status";

  /**
   * Property key for "operations"
   */
  public static final String PROPERTY_OPERATIONS = "operations";

  /**
   * Property key for "transforms"
   */
  public static final String PROPERTY_TRANSFORMS = "transforms";

  /**
   * Property key for "modified", the value for the property should indicate how the entity was modified
   */
  public static final String PROPERTY_MODIFIED = "modified";

  /**
   * Property key for "kettleType", the value for the property should indicate the
   * textual representation of the data type used in kettle
   */
  public static final String PROPERTY_KETTLE_TYPE = "kettleType";

  /**
   * Property key for the "logicalId", the value for the property should represent what logically identifies this node
   * as unique
   */
  public static final String PROPERTY_LOGICAL_ID = "logicalId";

  /**
   * Property key for the "hostName"
   */
  public static final String PROPERTY_HOST_NAME = "hostName";

  /**
   * Property key for the "hostName"
   */
  public static final String PROPERTY_USER_NAME = "userName";

  /**
   * Property key for the "hostName"
   */
  public static final String PROPERTY_PORT = "port";

  /**
   * Property key for namespace, the value for the property should represent the namespace that isolates this node
   */
  public static final String PROPERTY_NAMESPACE = "namespace";

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
   * Label for an "is read by" edge in the graph, e.g. a text file is read by a transformation step
   */
  public static final String LINK_READBY = "isreadby";

  /**
   * Label for a "uses" edge in the graph, e.g. a table output step uses an incoming transformation stream field to
   * populate a database column.
   */
  public static final String LINK_USES = "uses";

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
   * Label for an "deletes" edge in the graph, e.g. a select values step can remove a field from the stream
   */
  public static final String LINK_DELETES = "deletes";

  /**
   * Label for an "hops" edge in the graph, e.g. a step sends data to another step via a "hop"
   */
  public static final String LINK_HOPSTO = "hops_to";

  /**
   * Label for an "derives" edge in the graph, incoming transformation field(s)
   * can contribute to (or derive) the value in outgoing field(s)
   */
  public static final String LINK_DERIVES = "derives";

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
   * A node that represents an execution
   */
  public static final String NODE_TYPE_RUNTIME = "RUNTIME";

  /**
   * Node that represents the application that executes a runtime set of metadata
   */
  public static final String NODE_TYPE_EXECUTION_ENGINE = "Execution Engine";

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
   * The static context refers to analysis of documents that does not produce runtime information
   */
  public static final String CONTEXT_STATIC = "static";

  /**
   * A runtime context refers to analyis of documents that produces runtime information (parameter values, e.g.)
   */
  public static final String CONTEXT_RUNTIME = "runtime";

  /**
   * The default context for metaverse descriptors
   */
  public static final String CONTEXT_DEFAULT = CONTEXT_STATIC;

  public static final ILogicalIdGenerator LOGICAL_ID_GENERATOR_DEFAULT = new MetaverseLogicalIdGenerator( new String[] {
    PROPERTY_NAMESPACE,
    PROPERTY_TYPE,
    PROPERTY_NAME,
  } );

  public static final ILogicalIdGenerator LOGICAL_ID_GENERATOR_FILE = new MetaverseLogicalIdGenerator( new String[] {
    PROPERTY_PATH,
    PROPERTY_NAMESPACE
  } );

  public static final ILogicalIdGenerator LOGICAL_ID_GENERATOR_DB_JDBC = new MetaverseLogicalIdGenerator( new String[] {
    PROPERTY_TYPE,
    PROPERTY_HOST_NAME,
    PROPERTY_USER_NAME,
    PROPERTY_PORT,
    "accessTypeDesc"
  } );

  public static final ILogicalIdGenerator LOGICAL_ID_GENERATOR_DB_JNDI = new MetaverseLogicalIdGenerator( new String[] {
    PROPERTY_TYPE,
    PROPERTY_NAME,
    "accessTypeDesc"
  } );

  public static final ILogicalIdGenerator LOGICAL_ID_GENERATOR_LOCATOR = new MetaverseLogicalIdGenerator( new String[] {
    PROPERTY_TYPE,
    PROPERTY_NAME
  } );

  public static final ILogicalIdGenerator LOGICAL_ID_GENERATOR_DOCUMENT = new MetaverseLogicalIdGenerator( new String[] {
    PROPERTY_TYPE,
    PROPERTY_PATH,
    PROPERTY_NAMESPACE
  } );

  /**
   * Hides the constructor so that this class cannot be instanced
   */
  protected DictionaryConst() {
    throw new UnsupportedOperationException();
  }

}

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

  /*
   * This is a set of known content types
   */
  public static final String NODE_TYPE_LOCATOR = "Locator";
  // TODO temporary; need to get the demo working; permanent fix in progress
  public static final String NODE_TYPE_JOB = "kjb";
  public static final String NODE_TYPE_TRANS = "ktr";
  public static final String NODE_TYPE_TRANS_STEP = "Transformation Step";
  public static final String NODE_TYPE_JOB_ENTRY = "Job Entry";
  public static final String NODE_TYPE_DATASOURCE = "Database Connection";
  public static final String NODE_TYPE_DATA_TABLE = "Database Table";
  public static final String NODE_TYPE_TRANS_FIELD = "Transformation Stream field";
  public static final String NODE_TYPE_DATA_COLUMN = "Database Column";
  public static final String NODE_TYPE_FILE = "File";
  public static final String NODE_TYPE_FILE_FIELD = "File Field";
  public static final String NODE_TYPE_WEBSERVICE = "Web Service";
  public static final String NODE_TYPE_LOGICAL_MODEL = "Logical Model";
  public static final String NODE_TYPE_LOGICAL_HIERARCHY = "Logical Hierarchy";
  public static final String NODE_TYPE_LOGICAL_FIELD = "Model Field";
  public static final String NODE_TYPE_USER_CONTENT = "User Content";
  public static final String NODE_TYPE_ENTITY = "Entity";
  public static final String NODE_TYPE_ROOT_ENTITY = "Root_Entity";

  public static final String NODE_VIRTUAL = "virtual";

  public static final String CATEGORY_ABSTRACT = "abstract";
  public static final String CATEGORY_DATASOURCE = "datasource";
  public static final String CATEGORY_DOCUMENT = "document";
  public static final String CATEGORY_DOCUMENT_ELEMENT = "documentelement";
  public static final String CATEGORY_FIELD = "field";
  public static final String CATEGORY_FIELD_COLLECTION = "collection";
  public static final String CATEGORY_REPOSITORY = "repository";
  public static final String CATEGORY_OTHER = "other";

  public static final String COLOR_ABSTRACT = "#dddddd";
  public static final String COLOR_DATASOURCE = "#ff6600";
  public static final String COLOR_DOCUMENT = "#ccffcc";
  public static final String COLOR_DOCUMENT_ELEMENT = "#ccffff";
  public static final String COLOR_FIELD = "#ffcc99";
  public static final String COLOR_FIELD_COLLECTION = "#ff9900";
  public static final String COLOR_REPOSITORY = "#66aa44";
  public static final String COLOR_OTHER = "#ffcccc";

}

package com.pentaho.dictionary;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

/**
 * A helper class for the Pentaho Dictionary
 *
 * @author jdixon
 */
@SuppressWarnings( "rawtypes" )
public class DictionaryHelper {

  private static Map<String, IIdGenerator> keyIdGeneratorMap = new HashMap<String, IIdGenerator>();

  private static Map<Class, IIdGenerator> classIdGeneratorMap = new HashMap<Class, IIdGenerator>();
  private static List<Class> classList = new ArrayList<Class>();
  private static Map<String, String> categoryColorMap = new HashMap<String, String>();
  private static Map<String, String> typeCategoryMap = new HashMap<String, String>();

  // TODO This is temporary
  static {
    DictionaryHelper.addIdGenerator(null, new HashSet<Class>() {
      {
        add( TransMeta.class );
      }
    }, new GenericIdGenerator( DictionaryConst.NODE_TYPE_TRANS ) );

    DictionaryHelper.addIdGenerator(null, new HashSet<Class>() {
      {
        add( JobMeta.class );
      }
    }, new GenericIdGenerator( DictionaryConst.NODE_TYPE_JOB ) );

    DictionaryHelper.addIdGenerator(null, new HashSet<Class>() {
      {
        add( StepMeta.class );
        add( BaseStepMeta.class );
        add( TableOutputMeta.class );
        add( TextFileOutputMeta.class );
      }
    }, new GenericIdGenerator( DictionaryConst.NODE_TYPE_TRANS_STEP ) );

    DictionaryHelper.addIdGenerator(null, new HashSet<Class>() {
      {
        add( JobEntryInterface.class );
      }
    }, new GenericIdGenerator( DictionaryConst.NODE_TYPE_JOB_ENTRY ) );

    DictionaryHelper.addIdGenerator(null, new HashSet<Class>() {
      {
        add( DatabaseMeta.class );
      }
    }, new GenericIdGenerator( DictionaryConst.NODE_TYPE_DATASOURCE ) );

    DictionaryHelper.addIdGenerator(
        new HashSet<String>() {
          {
            add( DictionaryConst.NODE_TYPE_TRANS_FIELD );
          }
        },
        null,
        new GenericIdGenerator( DictionaryConst.NODE_TYPE_TRANS_FIELD ) );

    DictionaryHelper.addIdGenerator(
        new HashSet<String>() {
          {
            add( DictionaryConst.NODE_TYPE_DATA_TABLE );
          }
        },
        null,
        new GenericIdGenerator( DictionaryConst.NODE_TYPE_DATA_TABLE ) );

    DictionaryHelper.addIdGenerator(
        new HashSet<String>() {
          {
            add( DictionaryConst.NODE_TYPE_DATA_COLUMN );
          }
        },
        null,
        new GenericIdGenerator( DictionaryConst.NODE_TYPE_DATA_COLUMN ) );

    DictionaryHelper.addIdGenerator(
      new HashSet<String>() {
        {
          add( DictionaryConst.NODE_TYPE_FILE );
        }
      },
      null,
      new GenericIdGenerator( DictionaryConst.NODE_TYPE_FILE ) );

    DictionaryHelper.addIdGenerator(
      new HashSet<String>() {
        {
          add( DictionaryConst.NODE_TYPE_FILE_FIELD );
        }
      },
      null,
      new GenericIdGenerator( DictionaryConst.NODE_TYPE_FILE_FIELD ) );

  }

  /**
   * The set of structural link types
   */
  public static final Set<String> STRUCTURAL_LINK_TYPES = new HashSet<String>();

  /**
   * The set of entity node types
   */
  public static final Set<String> ENTITY_NODE_TYPES = new HashSet<String>();

  /**
   * The set of data flow link types
   */
  public static final Set<String> DATAFLOW_LINK_TYPES = new HashSet<String>();

  /**
   * Registers a new entity type, e.g. "ktr", or "logicalmodel"
   *
   * @param entityType
   */
  public static void registerEntityType( String entityType ) {
    ENTITY_NODE_TYPES.add( entityType );
  }

  /**
   * Registers a new structural link, e.g. "defines", or "contains"
   *
   * @param linkType
   */
  public static void registerStructuralLinkType( String linkType ) {
    STRUCTURAL_LINK_TYPES.add( linkType );
  }

  /**
   * Registers a new data flow link, e.g. "populates"
   *
   * @param linkType
   */
  public static void registerDataFlowLinkType( String linkType ) {
    DATAFLOW_LINK_TYPES.add( linkType );
  }

  /**
   * Returns true if the link type is structural in nature, e.g. "contains"
   *
   * @param linkType The type of the link
   * @return True if the link is structural
   */
  public static boolean isStructuralLinkType( String linkType ) {
    return STRUCTURAL_LINK_TYPES.contains( linkType );
  }

  /**
   * Returns true if the link type is data flow in nature, e.g. "populates"
   *
   * @param linkType The type of the link
   * @return True if the link is data flow
   */
  public static boolean isDataFlowLinkType( String linkType ) {
    return DATAFLOW_LINK_TYPES.contains( linkType );
  }

  /**
   * Returns true if the node type is an entity, e.g. "ktr"
   *
   * @param nodeType The type of the node
   * @return True if the node is an entity
   */
  public static boolean isEntityType( String nodeType ) {
    return ENTITY_NODE_TYPES.contains( nodeType );
  }

  static {
    registerStructuralLinkType( DictionaryConst.LINK_EXECUTES );
    registerStructuralLinkType( DictionaryConst.LINK_CONTAINS );
    registerStructuralLinkType( DictionaryConst.LINK_DEFINES );
    registerStructuralLinkType( DictionaryConst.LINK_PARENT_CONCEPT );

    registerDataFlowLinkType( DictionaryConst.LINK_POPULATES );
    registerDataFlowLinkType( DictionaryConst.LINK_READBY );
    registerDataFlowLinkType( DictionaryConst.LINK_WRITESTO );
    registerDataFlowLinkType( DictionaryConst.LINK_DEPENDENCYOF );

    registerEntityType( DictionaryConst.NODE_TYPE_DATASOURCE );
    registerEntityType( DictionaryConst.NODE_TYPE_DATA_TABLE );
    registerEntityType( DictionaryConst.NODE_TYPE_DATA_COLUMN );
    registerEntityType( DictionaryConst.NODE_TYPE_JOB );
    registerEntityType( DictionaryConst.NODE_TYPE_JOB_ENTRY );
    registerEntityType( DictionaryConst.NODE_TYPE_LOGICAL_MODEL );
    registerEntityType( DictionaryConst.NODE_TYPE_TRANS );
    registerEntityType( DictionaryConst.NODE_TYPE_TRANS_STEP );
    registerEntityType( DictionaryConst.NODE_TYPE_TRANS_FIELD );
    registerEntityType( DictionaryConst.NODE_TYPE_USER_CONTENT );

    categoryColorMap.put( DictionaryConst.CATEGORY_ABSTRACT, DictionaryConst.COLOR_ABSTRACT );
    categoryColorMap.put( DictionaryConst.CATEGORY_DATASOURCE, DictionaryConst.COLOR_DATASOURCE );
    categoryColorMap.put( DictionaryConst.CATEGORY_DOCUMENT, DictionaryConst.COLOR_DOCUMENT );
    categoryColorMap.put( DictionaryConst.CATEGORY_DOCUMENT_ELEMENT, DictionaryConst.COLOR_DOCUMENT_ELEMENT );
    categoryColorMap.put( DictionaryConst.CATEGORY_FIELD, DictionaryConst.COLOR_FIELD );
    categoryColorMap.put( DictionaryConst.CATEGORY_FIELD_COLLECTION, DictionaryConst.COLOR_FIELD_COLLECTION );
    categoryColorMap.put( DictionaryConst.CATEGORY_REPOSITORY, DictionaryConst.COLOR_REPOSITORY );
    categoryColorMap.put( DictionaryConst.CATEGORY_OTHER, DictionaryConst.COLOR_OTHER );

    typeCategoryMap.put( DictionaryConst.NODE_TYPE_DATASOURCE, DictionaryConst.CATEGORY_DATASOURCE );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_DATA_TABLE, DictionaryConst.CATEGORY_FIELD_COLLECTION );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_DATA_COLUMN, DictionaryConst.CATEGORY_FIELD );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_JOB, DictionaryConst.CATEGORY_DOCUMENT );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_JOB_ENTRY, DictionaryConst.CATEGORY_DOCUMENT_ELEMENT );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_TRANS, DictionaryConst.CATEGORY_DOCUMENT );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_TRANS_STEP, DictionaryConst.CATEGORY_DOCUMENT_ELEMENT );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_TRANS_FIELD, DictionaryConst.CATEGORY_FIELD );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_ENTITY, DictionaryConst.CATEGORY_ABSTRACT );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_FILE, DictionaryConst.CATEGORY_FIELD_COLLECTION );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_FILE_FIELD, DictionaryConst.CATEGORY_FIELD );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_LOCATOR, DictionaryConst.CATEGORY_REPOSITORY );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_ROOT_ENTITY, DictionaryConst.CATEGORY_ABSTRACT );
    typeCategoryMap.put( DictionaryConst.NODE_TYPE_WEBSERVICE, DictionaryConst.CATEGORY_DATASOURCE );

  }

  /**
   * Returns the category id for a given node type. If the node type is not of a known category,
   * a category of "other" will be returned
   * 
   * @param type The type for which the category is needed
   * @return The category
   */
  public static String getCategoryForType( String type ) {
    String category = typeCategoryMap.get( type );
    if ( category == null ) {
      category = DictionaryConst.CATEGORY_OTHER;
    }
    return category;
  }

  /**
   * Returns the suggested color for a given category. If the category does not have a known color,
   * a the color for the "other" category will be returned.
   * 
   * @param category The category for which the color is needed
   * @return The color
   */
  public static String getColorForCategory( String category ) {
    String color = categoryColorMap.get( category );
    if ( color == null ) {
      color = categoryColorMap.get( DictionaryConst.CATEGORY_OTHER );
    }
    return color;
  }

  /**
   * Adds an Id generator to the dictionary. Id generators may be looked up using a 
   * string token (e.g. "ktr"), a Class (e.g. Trans) or an object (instance of a Trans).
   *
   * @param types The string tokens that can be used to access this id generator. Can be null.
   * @param classes The Classes that can be used to access this id generator. Can be null.
   * @param idGenerator The id generator
   */
  public static void addIdGenerator( Set<String> types, Set<Class> classes, IIdGenerator idGenerator ) {

    if ( types != null ) {
      for ( String type : types ) {
        keyIdGeneratorMap.put( type, idGenerator );
      }
    }
    if ( classes != null ) {
      for ( Class clazz : classes ) {
        classIdGeneratorMap.put( clazz, idGenerator );
        classList.add( clazz );
      }
    }
  }

  /**
   * Returns an id generator based on a string token
   *
   * @param type The string to use for the lookup
   * @param tokens The tokens to use in the id generation
   * @return The id. This will be null if the id generator was not found.
   */
  public static String getId( String type, String... tokens ) {
    IIdGenerator idGen = keyIdGeneratorMap.get( type );
    if ( idGen != null ) {
      return idGen.getId( tokens );
    }
    return null;
  }

  /**
   * Returns an id generator based on a Class
   *
   * @param clazz The Class to use for the lookup
   * @param tokens The tokens to use in the id generation
   * @return The id. This will be null if the id generator was not found.
   */
  public static String getId( Class clazz, String... tokens ) {
    IIdGenerator idGen = classIdGeneratorMap.get( clazz );
    if ( idGen != null ) {
      return idGen.getId( tokens );
    }
    return null;
  }

  /**
   * Returns an id generator based on an object. If the object is an instance of any Class
   * that has an id generator, that id generator will be returned. The first match will be
   * returned, not necessarily the best or closest match.
   *
   * @param obj The Object to use for the lookup
   * @param tokens The tokens to use in the id generation
   * @return The id. This will be null if the id generator was not found.
   */
  public static String getId( Object obj, String... tokens ) {
    for ( Class clazz : classList ) {
      if ( clazz.isInstance( obj ) ) {
        IIdGenerator idGen = classIdGeneratorMap.get( clazz );
        return idGen.getId( tokens );
      }
    }
    return null;
  }

  /**
   * Creates an in-memory metaverse node from the provided parameters
   *
   * @param id The id of the node. An IIdGenerator should be used to create this.
   * @param name The name of the node
   * @param type The type of the node
   * @param properties The properties of the node
   * @return The metaverse node
   */
  public static IMetaverseNode createMetaverseNode( String id, String name, String type, Properties properties ) {
    MetaverseTransientNode node = new MetaverseTransientNode();
    node.setStringID( id );
    node.setType( type );
    node.setName( name );
    if ( properties != null ) {
      Enumeration<?> propertyNames = properties.propertyNames();
      while ( propertyNames.hasMoreElements() ) {
        Object propertyName = propertyNames.nextElement();
        Object value = properties.get( propertyName );
        node.setProperty( propertyName.toString(), value );
      }
    }
    return node;
  }

  /**
   * Creates a child node of a metaverse node and populates it with the provided parameters.
   * The relationship should be one of DictionaryConst.LINK_*
   *
   * @param id The id of the node. An IIdGenerator should be used to create this.
   * @param name The name of the node
   * @param type The type of the node
   * @param properties The properties of the node
   * @param parent The parent node
   * @param relationship The type of parent-child relationship
   * @return The metaverse node
   */
  public static IMetaverseNode addChildNode( String id, String name, String type,
      Properties properties, IMetaverseNode parent, String relationship ) {
    MetaverseTransientNode child = (MetaverseTransientNode) DictionaryHelper.createMetaverseNode(
        id, name, type, properties );
    if ( parent instanceof MetaverseTransientNode ) {
      MetaverseLink link = new MetaverseLink( parent, relationship, child );
      ( (MetaverseTransientNode) parent ).addLink( link );
    }
    return child;
  }

}

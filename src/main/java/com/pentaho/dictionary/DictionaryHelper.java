package com.pentaho.dictionary;


import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java .util.Set;
import java.util.Enumeration;
import java.util.Properties;

/**
 * A helper class for the Pentaho Dictionary
 * @author jdixon
 *
 */
@SuppressWarnings( "rawtypes" )
public class DictionaryHelper {

  private static Map<String, IIdGenerator> keyIdGeneratorMap = new HashMap<String, IIdGenerator>();

  private static Map<Class, IIdGenerator> classIdGeneratorMap = new HashMap<Class, IIdGenerator>();
  private static List<Class> classList = new ArrayList<Class>();

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
        add( TableOutputMeta.class );
      }
    }, new GenericIdGenerator( DictionaryConst.NODE_TYPE_TRANS_STEP ) );

    DictionaryHelper.addIdGenerator(null, new HashSet<Class>() {
      {
        add( TransMeta.class );
      }
    }, new GenericIdGenerator( DictionaryConst.NODE_TYPE_TRANS ) );

    DictionaryHelper.addIdGenerator(null, new HashSet<Class>() {
      {
        add( JobEntryCopy.class );
      }
    }, new GenericIdGenerator( DictionaryConst.NODE_TYPE_JOB_ENTRY ) );

    DictionaryHelper.addIdGenerator(null, new HashSet<Class>() {
      {
        add( DatabaseMeta.class );
      }
    }, new GenericIdGenerator( DictionaryConst.NODE_TYPE_DATASOURCE ) );

    DictionaryHelper.addIdGenerator(new HashSet<String>() {
      {
        add( DictionaryConst.NODE_TYPE_TRANS_FIELD );
      }
    }, null,
      new GenericIdGenerator( DictionaryConst.NODE_TYPE_TRANS_FIELD ) );

    DictionaryHelper.addIdGenerator(new HashSet<String>() {
      {
        add( DictionaryConst.NODE_TYPE_DATA_TABLE );
      }
    }, null,
      new GenericIdGenerator( DictionaryConst.NODE_TYPE_DATA_TABLE ) );
  }

  /**
   * Adds an Id generator to the dictionary. Id generators may be looked up using a 
   * string token (e.g. "ktr"), a Class (e.g. Trans) or an object (instance of a Trans).
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

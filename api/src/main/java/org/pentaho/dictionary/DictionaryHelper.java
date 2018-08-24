/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.dictionary;

import org.apache.commons.lang.StringUtils;
import org.pentaho.metaverse.api.IMetaverseNode;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.pentaho.dictionary.DictionaryConst.*;

/**
 * A helper class for the Hitachi Vantara Dictionary
 *
 * @author jdixon
 */
@SuppressWarnings( "rawtypes" )
public class DictionaryHelper {

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

  private static Map<String, String> categoryColorMap = new HashMap<String, String>();
  private static Map<String, String> typeCategoryMap = new HashMap<String, String>();
  private static Map<String, Map<String, String>> entityTypeLinks = new HashMap<>();

  /**
   * Hides the constructor so that this class cannot be instanced
   */
  protected DictionaryHelper() {
    throw new UnsupportedOperationException();
  }

  /**
   * Registers a new entity type, e.g. "ktr", or "logicalmodel"
   *
   * @param entityType The entity type
   */
  public static void registerEntityType( String entityType ) {
    registerEntityType( null, entityType, null );
  }

  /**
   * Registers the node {@code entityType} as a known entity node, along with its link to a potential parent entity
   * node. Note that {@code parentEntityType} can be {@code null}, which signifies a link to the root node.
   *
   * @param linkType         the type of link between the {@code entityType} and {@code parentEntityType}
   * @param entityType       the entity node being registered
   * @param parentEntityType the parent entity node to which the node is linked - a value of {@code null} signifies a
   *                         link to the root node
   */
  public static void registerEntityType(
    final String linkType, final String entityType, final String parentEntityType ) {
    if ( linkType != null ) {
      // are any links of this type already registered?
      Map<String, String> links = entityTypeLinks.get( linkType );
      if ( links == null ) {
        // this is the first link of this type
        links = new HashMap<>();
        entityTypeLinks.put( linkType, links );
      }
      // register the link between of entityType and parentNodeType - allow null values intentionally to mark links to
      // the root node
      links.put( entityType, parentEntityType );
    }
    if ( !StringUtils.isBlank( entityType ) ) {
      ENTITY_NODE_TYPES.add( entityType );
    }
  }

  /**
   * Registers a new structural link, e.g. "defines", or "contains"
   *
   * @param linkType The link type
   */
  public static void registerStructuralLinkType( String linkType ) {
    STRUCTURAL_LINK_TYPES.add( linkType );
  }

  /**
   * Registers a new data flow link, e.g. "populates"
   *
   * @param linkType The link type
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
    registerStructuralLinkType( LINK_EXECUTES );
    registerStructuralLinkType( LINK_CONTAINS );
    registerStructuralLinkType( LINK_DEFINES );
    registerStructuralLinkType( LINK_PARENT_CONCEPT );

    registerDataFlowLinkType( LINK_POPULATES );
    registerDataFlowLinkType( LINK_READBY );
    registerDataFlowLinkType( LINK_WRITESTO );
    registerDataFlowLinkType( LINK_DERIVES );
    registerDataFlowLinkType( LINK_DEPENDENCYOF );

    categoryColorMap.put( CATEGORY_ABSTRACT, COLOR_ABSTRACT );
    categoryColorMap.put( CATEGORY_DATASOURCE, COLOR_DATASOURCE );
    categoryColorMap.put( CATEGORY_DOCUMENT, COLOR_DOCUMENT );
    categoryColorMap.put( CATEGORY_DOCUMENT_ELEMENT, COLOR_DOCUMENT_ELEMENT );
    categoryColorMap.put( CATEGORY_FIELD, COLOR_FIELD );
    categoryColorMap.put( CATEGORY_FIELD_COLLECTION, COLOR_FIELD_COLLECTION );
    categoryColorMap.put( CATEGORY_REPOSITORY, COLOR_REPOSITORY );
    categoryColorMap.put( CATEGORY_OTHER, COLOR_OTHER );

    typeCategoryMap.put( NODE_TYPE_DATASOURCE, CATEGORY_DATASOURCE );
    typeCategoryMap.put( NODE_TYPE_DATA_TABLE, CATEGORY_FIELD_COLLECTION );
    typeCategoryMap.put( NODE_TYPE_DATA_COLUMN, CATEGORY_FIELD );
    typeCategoryMap.put( NODE_TYPE_JOB, CATEGORY_DOCUMENT );
    typeCategoryMap.put( NODE_TYPE_JOB_ENTRY, CATEGORY_DOCUMENT_ELEMENT );
    typeCategoryMap.put( NODE_TYPE_TRANS, CATEGORY_DOCUMENT );
    typeCategoryMap.put( NODE_TYPE_TRANS_STEP, CATEGORY_DOCUMENT_ELEMENT );
    typeCategoryMap.put( NODE_TYPE_TRANS_FIELD, CATEGORY_FIELD );
    typeCategoryMap.put( NODE_TYPE_ENTITY, CATEGORY_ABSTRACT );
    typeCategoryMap.put( NODE_TYPE_FILE, CATEGORY_FIELD_COLLECTION );
    typeCategoryMap.put( NODE_TYPE_FILE_FIELD, CATEGORY_FIELD );
    typeCategoryMap.put( NODE_TYPE_LOCATOR, CATEGORY_REPOSITORY );
    typeCategoryMap.put( NODE_TYPE_ROOT_ENTITY, CATEGORY_ABSTRACT );
    typeCategoryMap.put( NODE_TYPE_WEBSERVICE, CATEGORY_DATASOURCE );
  }

  public static void registerEntityTypes() {

    // register all used entity nodes and add appropriate links between them and the nodes that they are related to
    registerEntityType( LINK_PARENT_CONCEPT, NODE_TYPE_EXTERNAL_CONNECTION, null );
    registerEntityType( LINK_PARENT_CONCEPT, NODE_TYPE_DATASOURCE, NODE_TYPE_EXTERNAL_CONNECTION );
    registerEntityType( LINK_PARENT_CONCEPT, NODE_TYPE_DATA_TABLE, null );
    registerEntityType( LINK_PARENT_CONCEPT, NODE_TYPE_DATA_COLUMN, null );
    registerEntityType( LINK_CONTAINS_CONCEPT, NODE_TYPE_DATA_COLUMN, NODE_TYPE_DATA_TABLE );
    registerEntityType( LINK_PARENT_CONCEPT, NODE_TYPE_MONGODB_CONNECTION, NODE_TYPE_EXTERNAL_CONNECTION );
    registerEntityType( LINK_PARENT_CONCEPT, NODE_TYPE_MONGODB_COLLECTION, null );
    registerEntityType( LINK_PARENT_CONCEPT, NODE_TYPE_JOB, null );
    registerEntityType( LINK_CONTAINS_CONCEPT, NODE_TYPE_JOB_ENTRY, NODE_TYPE_JOB );
    registerEntityType( LINK_PARENT_CONCEPT, NODE_TYPE_LOGICAL_MODEL, null );
    registerEntityType( LINK_PARENT_CONCEPT, NODE_TYPE_TRANS, null );
    registerEntityType( LINK_CONTAINS_CONCEPT, NODE_TYPE_TRANS_STEP, NODE_TYPE_TRANS );
    registerEntityType( LINK_CONTAINS_CONCEPT, NODE_TYPE_TRANS_FIELD, NODE_TYPE_TRANS_STEP );
    registerEntityType( LINK_PARENT_CONCEPT, NODE_TYPE_USER_CONTENT, null );
    registerEntityType( LINK_PARENT_CONCEPT, NODE_TYPE_FILE, null );
    registerEntityType( LINK_CONTAINS_CONCEPT, NODE_TYPE_FILE_FIELD, NODE_TYPE_FILE );

  }

  /**
   * Returns the link type for a relationship between concrete data nodes (non-entity types) and the entity nodes that
   * represents their type.
   *
   * @return the link type for a relationship between concrete data nodes (non-entity types) and the entity nodes that
   * represents their type
   */
  public static String getNonEntityToEntityLinkType() {
    return LINK_TYPE_CONCEPT;
  }

  /**
   * Determines whether the links between concrete data nodes (non-entity types) and the entity nodes that represent
   * their types should have labels.
   *
   * @return true if the links between concrete data nodes (non-entity types) and the entity nodes that represent their
   * types should have labels
   */
  public static boolean addNonEntityToEntityLinkTypeLabel() {
    return true;
  }

  /**
   * Returns a {@link Set} of currently registered link types.
   *
   * @return a {@link Set} of currently registered link types
   */
  public static Set<String> getEntityLinkTypes() {
    return entityTypeLinks.keySet();
  }

  /**
   * Given the {@code linkType}, returns the name of the entity node that is expected to have a link of this type to
   * the node with the given {@code nodeName}.
   *
   * @param linkType the type of link being looked up for the given node
   * @param nodeName the node name whose parent is being looked up
   * @return the name of the entity node that is expected to have a link of type {@code linkType} to the node with the
   * given {@code nodeName}
   */
  public static String getParentEntityNodeType( final String linkType, final String nodeName ) {
    final Map<String, String> parentLinkMap = entityTypeLinks.get( linkType );
    if ( parentLinkMap != null ) {
      return parentLinkMap.get( nodeName );
    }
    return null;
  }

  /**
   * Returns true if the node with given {@code nodeName} has a link of type {@code linkType} to the root node.
   *
   * @param linkType the type of link being looked up for the given node
   * @param nodeName the name of the node whose relationship to the root node is being looked up
   * @return true if the node with given {@code nodeName} has a link of type {@code linkType} to the root node
   */
  public static boolean linksToRoot( final String linkType, final String nodeName ) {
    final Map<String, String> parentLinkMap = entityTypeLinks.get( linkType );
    if ( parentLinkMap != null ) {
      // value is null, but the key exists in the map - that means we should create a link of this type to the root
      return parentLinkMap.get( nodeName ) == null && parentLinkMap.containsKey( nodeName );
    }
    return false;
  }

  /**
   * Returns the category id for a given node type. If the node type is not of a known category, a category of "other"
   * will be returned
   *
   * @param type The type for which the category is needed
   * @return The category
   */
  public static String getCategoryForType( String type ) {
    String category = typeCategoryMap.get( type );
    if ( category == null ) {
      category = CATEGORY_OTHER;
    }
    return category;
  }

  /**
   * Returns the suggested color for a given category. If the category does not have a known color, a the color for the
   * "other" category will be returned.
   *
   * @param category The category for which the color is needed
   * @return The color
   */
  public static String getColorForCategory( String category ) {
    String color = categoryColorMap.get( category );
    if ( color == null ) {
      color = categoryColorMap.get( CATEGORY_OTHER );
    }
    return color;
  }

  /**
   * Creates an in-memory metaverse node from the provided parameters
   *
   * @param id         The id of the node. An IIdGenerator should be used to create this.
   * @param name       The name of the node
   * @param type       The type of the node
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
   * Creates a child node of a metaverse node and populates it with the provided parameters. The relationship should be
   * one of LINK_*
   *
   * @param id           The id of the node. An IIdGenerator should be used to create this.
   * @param name         The name of the node
   * @param type         The type of the node
   * @param properties   The properties of the node
   * @param parent       The parent node
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

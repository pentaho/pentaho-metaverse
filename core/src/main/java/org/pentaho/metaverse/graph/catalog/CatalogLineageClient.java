/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.graph.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pentaho.di.plugins.catalog.api.CatalogClient;
import com.pentaho.di.plugins.catalog.api.CatalogClientException;
import com.pentaho.di.plugins.catalog.api.authentication.AuthenticationType;
import com.pentaho.di.plugins.catalog.api.entities.DataResource;
import com.pentaho.di.plugins.catalog.api.entities.DataSource;
import com.pentaho.di.plugins.catalog.api.entities.search.Facet;
import com.pentaho.di.plugins.catalog.api.entities.search.PagingCriteria;
import com.pentaho.di.plugins.catalog.api.entities.search.SearchCriteria;
import com.pentaho.di.plugins.catalog.api.entities.search.SearchResult;
import com.pentaho.di.plugins.catalog.api.entities.search.SortBySpecs;
import com.pentaho.di.plugins.catalog.common.CatalogClientBuilderUtil;
import com.pentaho.di.plugins.catalog.provider.CatalogDetails;
import com.pentaho.di.plugins.catalog.read.ReadPayload;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.pentaho.di.plugins.catalog.api.entities.search.SearchCriteria.CATALOG_DEFAULT_LIMIT;

public class CatalogLineageClient {

  private static final Logger log = LogManager.getLogger( CatalogLineageClient.class );
  public static final String EDGES = "edges";

  private final String catalogUrl;
  private final String catalogUsername;
  private final String catalogPassword;
  private final String catalogTokenUrl;
  private final String catalogClientId;
  private final String catalogClientSecret;
  private static final String noTokenUrl = "${lineage.catalog.token.url}";

  public CatalogLineageClient( String catalogUrl,
                                  String catalogUsername,
                                  String catalogPassword,
                                  String catalogTokenUrl,
                                  String catalogClientId,
                                  String catalogClientSecret ) {
    this.catalogUrl = catalogUrl;
    this.catalogUsername = catalogUsername;
    this.catalogPassword = catalogPassword;
    this.catalogTokenUrl = catalogTokenUrl;
    this.catalogClientId = catalogClientId;
    this.catalogClientSecret = catalogClientSecret;
  }

  public void processLineage( List<LineageDataResource> inputSources, List<LineageDataResource> outputTargets ) {

    getExistingResourceIDs( inputSources );
    getExistingResourceIDs( outputTargets );

    if ( !hasCatalogIDs( inputSources ) || !hasCatalogIDs( outputTargets ) ) {
      log.info( "Either targets or sources IDs could not be found. Can't create lineage." );
      return;
    }

    outputTargets.forEach( dataResource -> {
      if ( dataResource.getCatalogResourceID() != null ) {
        deleteExistingLineageRelations( dataResource.getCatalogResourceID() );
      }
    } );

    outputTargets.forEach( outDataResource -> {
      if ( outDataResource.getCatalogResourceID() != null ) {
        inputSources.forEach( inputDataResource -> {
          if ( inputDataResource.getCatalogResourceID() != null ) {
            log.info( "Adding lineage relation  between " + inputDataResource.getCatalogResourceID() + " and " + outDataResource.getCatalogResourceID() + "." );
            addParentLineage( inputDataResource.getCatalogResourceID(), outDataResource.getCatalogResourceID() );
            log.info( "Lineage relation created between " + inputDataResource.getCatalogResourceID() + " and " + outDataResource.getCatalogResourceID() + "." );
          }
        } );
      }
    } );

    outputTargets.forEach( dataResource -> dataResource.getFieldLevelRelationships().forEach( fieldRelationship -> {
      log.info( "Create field relationship: " + fieldRelationship );
      addFieldLevelLineage( fieldRelationship );
    } ) );

  }

  private void getExistingResourceIDs( List<LineageDataResource> dataResources ) {
    dataResources.forEach( dataResource -> {
      try {
        populateDataResourceId( dataResource );
      } catch ( Exception e ) {
        log.error( "Error trying to populate resource id", e );
      }
    } );
  }

  private void populateDataResourceId( LineageDataResource lineageDataResource ) throws CatalogClientException {
    List<DataResource> catalogMatchResource = new ArrayList<>();
    log.debug( String.format( "Populating resource ID for %s", lineageDataResource ) );
    for ( DataResource dataResource : getResourcesByName( lineageDataResource.getName() ) ) {
      // loop through candidate matches and try to narrow down by path/data source
      if ( matchesByPathAndFile( lineageDataResource, dataResource )
        || matchesByDb( lineageDataResource, dataResource ) ) {
        catalogMatchResource.add( dataResource );
      }
    }

    if ( catalogMatchResource.size() == 1 ) {
      lineageDataResource.setCatalogResourceID( catalogMatchResource.get( 0 ).getKey() );
    } else if ( catalogMatchResource.isEmpty() ) {
      log.error( String.format( "Unable to find resource %s in catalog", lineageDataResource.getName() ) );
    } else {
      log.error( String.format( "Multiple matches for resource %s in catalog: ", lineageDataResource.getName() ) );
      for ( DataResource dataResource : catalogMatchResource ) {
        log.error( String.format( "key: %s path: %s tableName: %s schema: %s dbName: %s dataSource: %s",
          dataResource.getKey(), dataResource.getResourcePath(), dataResource.getTableName(),
          dataResource.getDataSchemaName(), dataResource.getDatabaseName(), dataResource.getDataSourceName() ) );
      }
    }
  }

  private boolean matchesByPathAndFile( LineageDataResource lineageDataResource, DataResource catalogDataResource ) {
    log.debug( String.format( "Checking catalog resource path %s", catalogDataResource.getResourcePath() ) );
    return null != lineageDataResource.getPath()
      && catalogDataResource.getResourcePath().endsWith( lineageDataResource.getName() );
    // TODO: uncomment this when running against a real LDOS cluster and using the same resources as in the catalog
      //&& catalogDataResource.getResourcePath().equals( lineageDataResource.getPath() );
  }

  private boolean matchesByDb( LineageDataResource lineageDataResource, DataResource catalogDataResource )
    throws CatalogClientException {
    if ( null != lineageDataResource.getDbHost() ) {
      DataSource dataSource = getCatalogClient().getDataSources().read( catalogDataResource.getDataSourceKey() );
      log.debug( String.format( "Checking catalog resource jdbcUrl %s, schemaName %s",
        dataSource.getJdbcUrl(), dataSource.getSourcePath() ) );
      return //dataSource.getJdbcUrl().contains( lineageDataResource.getDbHost() + ":" + lineageDataResource.getDbPort() )
       // &&
      dataSource.getSourcePath().equalsIgnoreCase( getCatalogDataSourcePath( lineageDataResource ) )
        && catalogDataResource.getResourcePath().equalsIgnoreCase( getCatalogResourcePath( lineageDataResource ) );
    } else {
      return false;
    }
  }

  private String getCatalogResourcePath( LineageDataResource lineageDataResource ) {
    return "/" + lineageDataResource.getDbSchema() + "." + lineageDataResource.getName();
  }

  private String getCatalogDataSourcePath( LineageDataResource lineageDataResource ) {
    return "/" + lineageDataResource.getDbSchema();
  }

  private List<DataResource> getResourcesByName( String resourceName ) {
    try {

      SearchCriteria.SearchCriteriaBuilder searchCriteriaBuilder = new SearchCriteria.SearchCriteriaBuilder();
      searchCriteriaBuilder.searchPhrase( resourceName );
      searchCriteriaBuilder.addFacet( Facet.RESOURCE_TAGS, "" );
      searchCriteriaBuilder.addFacet( Facet.VIRTUAL_FOLDERS, "" );
      searchCriteriaBuilder.addFacet( Facet.DATA_SOURCES, "" );
      searchCriteriaBuilder.addFacet( Facet.RESOURCE_TYPE, "" );
      searchCriteriaBuilder.addFacet( Facet.FILE_SIZE, "" );
      searchCriteriaBuilder.addFacet( Facet.FILE_FORMAT, "" );
      searchCriteriaBuilder.pagingCriteria( new PagingCriteria( 0, StringUtils.isNumeric( "1000" ) ? Integer.valueOf( "1000" ) : CATALOG_DEFAULT_LIMIT ) )
        .sortBySpecs( Collections.singletonList( new SortBySpecs( ReadPayload.SCORE, false ) ) )
        .entityScope( Collections.singletonList( ReadPayload.DATA_RESOURCE ) ).searchType( ReadPayload.ADVANCED )
        .preformedQuery( false ).showCollectionMembers( true ).build();

      CatalogClient catalogClient = getCatalogClient();
      return catalogClient.getSearch().doNew( searchCriteriaBuilder.build() ).getEntities();

    } catch ( CatalogClientException e ) {
      log.error( e.getMessage(), e );
      return new ArrayList<>();
    }
  }

  // deprecated; worked for protoype but needs to do more
  private String searchResourceByName( String resourceName ) {

    String resourceId = null;

    try {

      SearchCriteria.SearchCriteriaBuilder searchCriteriaBuilder = new SearchCriteria.SearchCriteriaBuilder();
      searchCriteriaBuilder.searchPhrase( resourceName );
      searchCriteriaBuilder.addFacet( Facet.RESOURCE_TAGS, "" );
      searchCriteriaBuilder.addFacet( Facet.VIRTUAL_FOLDERS, "" );
      searchCriteriaBuilder.addFacet( Facet.DATA_SOURCES, "" );
      searchCriteriaBuilder.addFacet( Facet.RESOURCE_TYPE, "" );
      searchCriteriaBuilder.addFacet( Facet.FILE_SIZE, "" );
      searchCriteriaBuilder.addFacet( Facet.FILE_FORMAT, "" );
      searchCriteriaBuilder.pagingCriteria( new PagingCriteria( 0, StringUtils.isNumeric( "1000" ) ? Integer.valueOf( "1000" ) : CATALOG_DEFAULT_LIMIT ) )
              .sortBySpecs( Collections.singletonList( new SortBySpecs( ReadPayload.SCORE, false ) ) )
              .entityScope( Collections.singletonList( ReadPayload.DATA_RESOURCE ) ).searchType( ReadPayload.ADVANCED )
              .preformedQuery( false ).showCollectionMembers( true ).build();

      CatalogClient catalogClient = getCatalogClient();
      SearchResult result = catalogClient.getSearch().doNew( searchCriteriaBuilder.build() );
      for ( DataResource dataResource : result.getEntities() ) {
        if ( dataResource.getResourcePath().endsWith( "/" + resourceName ) ) {
          resourceId = dataResource.getKey();
        }
      }

    } catch ( CatalogClientException e ) {
      log.error( e.getMessage(), e );
    }

    return resourceId;
  }

  private List<String> getFileLineageRelations( String resourceID ) {
    HashSet<String> lineageRelations = new HashSet<>();
    try {
      String getLineageInfo = "/api/v2/lineage/multihop/" + resourceID + "?fieldLineage=false&field=&upstream=1&downstream=0&lineageType=ACCEPTED%2CSUGGESTED";
      CatalogClient catalogClient = getCatalogClient();
      CloseableHttpResponse httpResponse = catalogClient.doGet( getLineageInfo );
      String jsonString = EntityUtils.toString( httpResponse.getEntity() );
      JSONObject jsonResponse = (JSONObject) new JSONParser().parse( jsonString );
      if ( jsonResponse.get( EDGES ) != null ) {
        ((JSONArray) jsonResponse.get( EDGES ) ).forEach( node -> {
          JSONObject jsonObject = (JSONObject) node;
          String type = (String) jsonObject.get( "type" );
          if ( type != null && type.equals( "edge" ) ) {
            String lineageRelationID = (String) jsonObject.get( "target" );
            lineageRelationID = lineageRelationID.substring( 0, lineageRelationID.lastIndexOf( "_" ) );
            lineageRelations.add( lineageRelationID );
          }
        } );
      }
    } catch ( IOException | CatalogClientException | ParseException e ) {
      log.error( e.getMessage(), e );
    }
    return new ArrayList<>( lineageRelations );
  }

  private List<String> getFieldLineageRelations( String resourceID, String fieldName ) {
    HashSet<String> lineageRelations = new HashSet<>();
    try {
      String getLineageInfo = "/api/v2/lineage/multihop/" + resourceID + "?fieldLineage=true&field=" + fieldName + "&upstream=1&downstream=0&lineageType=ACCEPTED,SUGGESTED";
      CatalogClient catalogClient = getCatalogClient();
      CloseableHttpResponse httpResponse = catalogClient.doGet( getLineageInfo );
      String jsonString = EntityUtils.toString( httpResponse.getEntity() );
      JSONObject jsonResponse = (JSONObject) new JSONParser().parse( jsonString );
      if ( jsonResponse.get( EDGES ) != null ) {
        ((JSONArray) jsonResponse.get( EDGES ) ).forEach( node -> {
          JSONObject jsonObject = (JSONObject) node;
          String type = (String) jsonObject.get( "type" );
          if ( type != null && type.equals( "edge" ) ) {
            String lineageRelationID = (String) jsonObject.get( "target" );
            lineageRelationID = lineageRelationID.substring( 0, lineageRelationID.lastIndexOf( "_" ) );
            String parentField = (String) jsonObject.get( "source" );
            parentField = parentField.substring( parentField.indexOf( "_" ) + 1, parentField.lastIndexOf( "_" ) );
            String parentID = (String) jsonObject.get( "source" );
            parentID = parentID.substring( 0, parentID.indexOf( "_" ) );
            lineageRelations.add( parentID + "|" + parentField + "|" + lineageRelationID );
          }
        } );
      }
    } catch ( IOException | CatalogClientException | ParseException e ) {
      log.error( e.getMessage(), e );
    }
    return new ArrayList<>( lineageRelations );
  }

  private void addParentLineage( String parentResourceID, String targetResourceID ) {

    try {
      List<String> existingLineageRelations = getFileLineageRelations( targetResourceID );
      String addParentURL = "/api/v2/lineage/addparent/" + targetResourceID;
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode childNode = objectMapper.createObjectNode();
      ((ObjectNode) childNode).put( "relatedKey", parentResourceID );
      if ( !existingLineageRelations.isEmpty() ) {
        String lineageRelationID = existingLineageRelations.get( 0 );
        ((ObjectNode) childNode).put( "operationExecutionKey", lineageRelationID );
        addParentURL = addParentURL + "?opKey=" + lineageRelationID;
      }
      String body = objectMapper.writeValueAsString( childNode );
      StringEntity entity = new StringEntity( body );
      CatalogClient catalogClient = getCatalogClient();
      CloseableHttpResponse httpResponse = catalogClient.doPost( addParentURL, entity );
      //System.out.println( "######################################################################################" );
      //System.out.println( EntityUtils.toString( httpResponse.getEntity() ) );
      //System.out.println( "######################################################################################" );
    } catch ( IOException | CatalogClientException e ) {
      log.error( e.getMessage(), e );
    }
  }

  private void addFieldLevelLineage( FieldLevelRelationship fieldRelationship ) {

    String targetResourceID = fieldRelationship.getOutputTargetResource().getCatalogResourceID();
    String targetFieldName = fieldRelationship.getOutputTargetResourceField();
    String parentResourceID = fieldRelationship.getInputSourceResource().getCatalogResourceID();
    String parentFieldName = fieldRelationship.getInputSourceResourceField();
    List<String> fileRelationships = getFileLineageRelations( targetResourceID );
    List<String> fieldRelationships = getFieldLineageRelations( targetResourceID, targetFieldName );
    boolean relationshipExists = false;
    HashSet<String> existingFieldRelations = new HashSet<>();

    for ( String relationship : fieldRelationships ) {
      String relParentResourceID = relationship.split( "\\|" )[0];
      String relParentField = relationship.split( "\\|" )[1];
      String relID = relationship.split( "\\|" )[2];

      existingFieldRelations.add( relID );
      if ( relParentResourceID.equals( parentResourceID ) && relParentField.equals( parentFieldName ) ) {
        relationshipExists = true;
      }
    }

    if ( relationshipExists ) {
      log.info( "Relationship already exists." );
      return;
    }

    try {
      String addFieldURL = "/api/v2/lineage/addparentfield/" + targetResourceID + "_" + targetFieldName;
      if ( !existingFieldRelations.isEmpty() ) {
        addFieldURL = addFieldURL + "?opKey=" + existingFieldRelations.iterator().next();
      }
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode childNode = objectMapper.createObjectNode();
      ((ObjectNode) childNode).put( "relatedKey", parentResourceID + "_" + parentFieldName );
      ((ObjectNode) childNode).put( "operationExecutionKey", fileRelationships.get( 0 ) );
      String body = objectMapper.writeValueAsString( childNode );
      StringEntity entity = new StringEntity( body );
      CatalogClient catalogClient = getCatalogClient();
      CloseableHttpResponse httpResponse = catalogClient.doPost( addFieldURL, entity );
      log.info( "Created field relationship: " + fieldRelationship );
    } catch ( IOException | CatalogClientException e ) {
      log.error( e.getMessage(), e );
    }

  }

  private void deleteExistingLineageRelations( String resourceID ) {
      getFileLineageRelations( resourceID ).forEach( this::deleteExistingLineageRelation );
  }

  private void deleteExistingLineageRelation( String lineageRelationID ) {
    try {
      String deleteLineageRelation = "/api/v2/lineage/" + lineageRelationID;
      CatalogClient catalogClient = getCatalogClient();
      catalogClient.doDelete( deleteLineageRelation );
      log.info( "Deleted old lineage relation " + lineageRelationID );
    } catch ( IOException | CatalogClientException e ) {
      log.error( e.getMessage(), e );
    }
  }

  private CatalogClient getCatalogClient() throws CatalogClientException {
    CatalogDetails catalogDetails = new CatalogDetails();
    AuthenticationType authenticationType = catalogTokenUrl.isEmpty() || noTokenUrl.equals( catalogTokenUrl ) ?
      AuthenticationType.BASIC_AUTH : AuthenticationType.OAUTH_2;
    catalogDetails.setAuthType( String.valueOf( authenticationType.getValue() ) );
    catalogDetails.setUrl( catalogUrl );
    catalogDetails.setUsername( catalogUsername );
    catalogDetails.setPassword( catalogPassword );
    if ( !AuthenticationType.BASIC_AUTH.equals( authenticationType ) ) {
      catalogDetails.setTokenUrl( catalogTokenUrl );
      catalogDetails.setClientId( catalogClientId );
      catalogDetails.setClientSecret( catalogClientSecret );
    }

    CatalogClient catalogClient;
    CatalogClientBuilderUtil catalogClientBuilderUtil = new CatalogClientBuilderUtil( catalogDetails );
    catalogClient = catalogClientBuilderUtil.getCatalogClient();
    catalogClient.login();

    return catalogClient;
  }

  private boolean hasCatalogIDs( List<LineageDataResource> dataResources ) {
    boolean catalogIDsFound = false;
    for ( LineageDataResource dataResource : dataResources ) {
      if ( !StringUtils.isEmpty( dataResource.getCatalogResourceID() ) ) {
        catalogIDsFound = true;
      }
    }
    return catalogIDsFound;
  }

  public boolean urlConfigured() {
    return null != catalogUrl && !catalogUrl.isEmpty()
      && !catalogUrl.equalsIgnoreCase( "${lineage.catalog.url}" );
  }
}

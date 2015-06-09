/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package org.pentaho.metaverse.impl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.trans.steps.mongodb.MongoDbMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.AnalysisContext;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.model.BaseResourceInfo;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

/**
 * User: RFellows Date: 3/10/15
 */
public class MongoDbResourceInfo extends BaseResourceInfo implements IExternalResourceInfo {

  public static final String JSON_PROPERTY_PORT = "port";
  public static final String JSON_PROPERTY_USERNAME = "username";
  public static final String JSON_PROPERTY_PASSWORD = "password";
  public static final String JSON_PROPERTY_DATABASE_NAME = "databaseName";
  public static final String JSON_PROPERTY_CONNECTION_TIMEOUT = "connectionTimeout";
  public static final String JSON_PROPERTY_HOST_NAMES = "hostNames";
  public static final String JSON_PROPERTY_SOCKET_TIMEOUT = "socketTimeout";
  public static final String JSON_PROPERTY_USE_ALL_REPLICA_SET_MEMBERS = "useAllReplicaSetMembers";
  public static final String JSON_PROPERTY_USE_KERBEROS_AUTHENTICATION = "useKerberosAuthentication";
  public static final String JSON_PROPERTY_COLLECTION = "collection";

  private String database;
  private String port;
  private String hostNames;
  private String user;
  private String password;
  private boolean useAllReplicaSetMembers;
  private boolean useKerberosAuthentication;
  private String connectTimeout;
  private String socketTimeout;
  private String collection;

  public MongoDbResourceInfo( MongoDbMeta mongoDbMeta ) {
    this( mongoDbMeta, new AnalysisContext( DictionaryConst.CONTEXT_RUNTIME ) );
  }

  public MongoDbResourceInfo( MongoDbMeta mongoDbMeta, IAnalysisContext context ) {
    setName( substituteIfNeeded( mongoDbMeta.getDbName(), mongoDbMeta, context ) );
    setDatabase( substituteIfNeeded( mongoDbMeta.getDbName(), mongoDbMeta, context ) );
    setPort( substituteIfNeeded( mongoDbMeta.getPort(), mongoDbMeta, context ) );
    setHostNames( substituteIfNeeded( mongoDbMeta.getHostnames(), mongoDbMeta, context ) );
    setUser( substituteIfNeeded( mongoDbMeta.getAuthenticationUser(), mongoDbMeta, context ) );
    setPassword( substituteIfNeeded( mongoDbMeta.getAuthenticationPassword(), mongoDbMeta, context ) );
    setUseAllReplicaSetMembers( mongoDbMeta.getUseAllReplicaSetMembers() );
    setUseKerberosAuthentication( mongoDbMeta.getUseKerberosAuthentication() );
    setConnectTimeout( substituteIfNeeded( mongoDbMeta.getConnectTimeout(), mongoDbMeta, context ) );
    setSocketTimeout( substituteIfNeeded( mongoDbMeta.getSocketTimeout(), mongoDbMeta, context ) );
    setCollection( substituteIfNeeded( mongoDbMeta.getCollection(), mongoDbMeta, context ) );
  }

  public MongoDbResourceInfo( String hostNames, String port, String database ) {
    setHostNames( hostNames );
    setPort( port );
    setDatabase( database );
  }

  private String substituteIfNeeded( String value, MongoDbMeta meta, IAnalysisContext context ) {
    String ret = context.equals( DictionaryConst.CONTEXT_RUNTIME )
      ? meta.getParentStepMeta().getParentTransMeta().environmentSubstitute( value ) : value;
    return ret;
  }

  @Override
  public String getType() {
    return "MongoDbResource";
  }

  @JsonProperty( JSON_PROPERTY_CONNECTION_TIMEOUT )
  public String getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout( String connectTimeout ) {
    this.connectTimeout = connectTimeout;
  }

  @JsonProperty( JSON_PROPERTY_DATABASE_NAME )
  public String getDatabase() {
    return database;
  }

  public void setDatabase( String database ) {
    this.database = database;
  }

  @JsonProperty( JSON_PROPERTY_HOST_NAMES )
  public String getHostNames() {
    return hostNames;
  }

  public void setHostNames( String hostNames ) {
    this.hostNames = hostNames;
  }

  @JsonProperty( JSON_PROPERTY_PORT )
  public String getPort() {
    return port;
  }

  public void setPort( String port ) {
    this.port = port;
  }

  @JsonProperty( JSON_PROPERTY_SOCKET_TIMEOUT )
  public String getSocketTimeout() {
    return socketTimeout;
  }

  public void setSocketTimeout( String socketTimeout ) {
    this.socketTimeout = socketTimeout;
  }

  @JsonProperty( JSON_PROPERTY_USE_ALL_REPLICA_SET_MEMBERS )
  public boolean isUseAllReplicaSetMembers() {
    return useAllReplicaSetMembers;
  }

  public void setUseAllReplicaSetMembers( boolean useAllReplicaSetMembers ) {
    this.useAllReplicaSetMembers = useAllReplicaSetMembers;
  }

  @JsonProperty( JSON_PROPERTY_USE_KERBEROS_AUTHENTICATION )
  public boolean isUseKerberosAuthentication() {
    return useKerberosAuthentication;
  }

  public void setUseKerberosAuthentication( boolean useKerberosAuthentication ) {
    this.useKerberosAuthentication = useKerberosAuthentication;
  }

  @JsonProperty( JSON_PROPERTY_USERNAME )
  public String getUser() {
    return user;
  }

  public void setUser( String user ) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  @JsonProperty( JSON_PROPERTY_PASSWORD )
  public void setPassword( String password ) {
    this.password = Encr.decryptPasswordOptionallyEncrypted( password );
  }

  @JsonProperty( JSON_PROPERTY_PASSWORD )
  protected String getEncryptedPassword() {
    // Need "Encrypted prefix for decryptPasswordOptionallyEncrypted() to operate properly
    return Encr.PASSWORD_ENCRYPTED_PREFIX + Encr.encryptPassword( password );
  }

  @JsonProperty( JSON_PROPERTY_COLLECTION )
  public String getCollection() {
    return collection;
  }

  public void setCollection( String collection ) {
    this.collection = collection;
  }
}

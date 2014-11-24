/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.impl.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.pentaho.metaverse.api.model.IArtifactMetadata;
import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.api.model.IParamInfo;
import com.pentaho.metaverse.api.model.IUserMetadata;

public class ExecutionData implements IExecutionData {
  private Timestamp startTime;
  private Timestamp endTime;
  private long failureCount = 0;
  private String executorServer;
  private String executorUser;
  private String clientExecutor;
  private String loggingChannelId;
  private List<IParamInfo<String>> parameters = new ArrayList<IParamInfo<String>>();
  private List<IExternalResourceInfo> externalResources = new ArrayList<IExternalResourceInfo>();
  private Map<Object, Object> variables = new Hashtable<Object, Object>();
  private List<Object> arguments = new ArrayList<Object>();
  private IArtifactMetadata artifactMetadata;
  private IUserMetadata userMetadata;
  
  public Timestamp getStartTime() {
    return startTime;
  }
  public void setStartTime( Timestamp startTime ) {
    this.startTime = startTime;
  }
  
  public Timestamp getEndTime() {
    return endTime;
  }
  public void setEndTime( Timestamp endTime ) {
    this.endTime = endTime;
  }
  
  public long getFailureCount() {
    return failureCount;
  }
  public void setFailureCount( long failureCount ) {
    this.failureCount = failureCount;
  }
  
  public String getExecutorServer() {
    return executorServer;
  }
  public void setExecutorServer( String executorServer ) {
    this.executorServer = executorServer;
  }
  
  public String getExecutorUser() {
    return executorUser;
  }
  
  public void setExecutorUser( String executorUser ) {
    this.executorUser = executorUser;
  }
  
  public String getClientExecutor() {
    return clientExecutor;
  }
  
  public void setClientExecutor( String clientExecutor ) {
    this.clientExecutor = clientExecutor;
  }
  
  public String getLoggingChannelId() {
    return loggingChannelId;
  }
  
  public void setLoggingChannelId( String loggingChannelId ) {
    this.loggingChannelId = loggingChannelId;
  }
  
  public List<IParamInfo<String>> getParameters() {
    return parameters;
  }
  
  public void setParameters( List<IParamInfo<String>> parameters ) {
    this.parameters = parameters;
  }

  public void addParameter( IParamInfo<String> parameter) {
    this.parameters.add( parameter );
  }
  
  public List<IExternalResourceInfo> getExternalResources() {
    return externalResources;
  }
  
  public void setExternalResources( List<IExternalResourceInfo> externalResources ) {
    this.externalResources = externalResources;
  }
  
  public void addExternalResource( IExternalResourceInfo externalResource) {
    this.externalResources.add( externalResource );
  }
  
  @Override
  public Map<Object, Object> getVariables() {
    return variables;
  }
  
  public void addVariable(String name, String value) {
    variables.put( name, value );
  }
  
  @Override
  public List<Object> getArguments() {
    return arguments;
  }
  
  public void putArgument(int index, Object value) {
    arguments.add( index, value );
  }
  
  public IArtifactMetadata getArtifactMetadata() {
    return artifactMetadata;
  }
  
  public IUserMetadata getUserMetadata() {
    return userMetadata;
  }

}

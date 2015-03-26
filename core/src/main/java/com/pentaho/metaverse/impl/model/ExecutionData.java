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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.pentaho.metaverse.api.model.IArtifactMetadata;
import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.api.model.IParamInfo;
import com.pentaho.metaverse.api.model.IUserMetadata;

public class ExecutionData implements IExecutionData {
  private Date startTime;
  private Date endTime;
  private long failureCount = 0;
  private String executorServer;
  private String executorUser;
  private String clientExecutor;
  private String loggingChannelId;
  private List<IParamInfo<String>> parameters = new ArrayList<IParamInfo<String>>();
  private Map<String, List<IExternalResourceInfo>> externalResources =
    new HashMap<String, List<IExternalResourceInfo>>();
  private Map<Object, Object> variables = new Hashtable<Object, Object>();
  private List<Object> arguments = new ArrayList<Object>();
  private IArtifactMetadata artifactMetadata;
  private IUserMetadata userMetadata;

  @Override
  public Date getStartTime() {
    return startTime;
  }

  @Override
  public void setStartTime( Date startTime ) {
    this.startTime = startTime;
  }

  @Override
  public Date getEndTime() {
    return endTime;
  }

  @Override
  public void setEndTime( Date endTime ) {
    this.endTime = endTime;
  }

  @Override
  public long getFailureCount() {
    return failureCount;
  }

  @Override
  public void setFailureCount( long failureCount ) {
    this.failureCount = failureCount;
  }

  @Override
  public String getExecutorServer() {
    return executorServer;
  }

  @Override
  public void setExecutorServer( String executorServer ) {
    this.executorServer = executorServer;
  }

  @Override
  public String getExecutorUser() {
    return executorUser;
  }

  @Override
  public void setExecutorUser( String executorUser ) {
    this.executorUser = executorUser;
  }

  @Override
  public String getClientExecutor() {
    return clientExecutor;
  }

  @Override
  public void setClientExecutor( String clientExecutor ) {
    this.clientExecutor = clientExecutor;
  }

  @Override
  public String getLoggingChannelId() {
    return loggingChannelId;
  }

  @Override
  public void setLoggingChannelId( String loggingChannelId ) {
    this.loggingChannelId = loggingChannelId;
  }

  @Override
  public List<IParamInfo<String>> getParameters() {
    return parameters;
  }

  @Override
  public void setParameters( List<IParamInfo<String>> parameters ) {
    this.parameters = parameters;
  }

  @Override
  public void addParameter( IParamInfo<String> parameter ) {
    this.parameters.add( parameter );
  }

  @Override
  public Map<String, List<IExternalResourceInfo>> getExternalResources() {
    return externalResources;
  }

  @Override
  public void setExternalResources( Map<String, List<IExternalResourceInfo>> externalResources ) {
    this.externalResources = externalResources;
  }

  @Override
  public void addExternalResource( String consumerName, IExternalResourceInfo externalResource ) {
    List<IExternalResourceInfo> resources = this.externalResources.get( consumerName );
    if ( resources == null ) {
      resources = new LinkedList<IExternalResourceInfo>();
    }
    resources.add( externalResource );
    this.externalResources.put( consumerName, resources );
  }

  @Override
  public Map<Object, Object> getVariables() {
    return variables;
  }

  @Override
  public void addVariable( String name, String value ) {
    variables.put( name, value );
  }

  @Override
  public List<Object> getArguments() {
    return arguments;
  }

  @Override
  public void putArgument( int index, Object value ) {
    arguments.add( index, value );
  }

  @Override
  public IArtifactMetadata getArtifactMetadata() {
    return artifactMetadata;
  }

  @Override
  public void setArtifactMetadata( IArtifactMetadata artifactMetadata ) {
    this.artifactMetadata = artifactMetadata;
  }

  @Override
  public IUserMetadata getUserMetadata() {
    return userMetadata;
  }

  @Override
  public void setUserMetadata( IUserMetadata userMetadata ) {
    this.userMetadata = userMetadata;
  }

}

/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse.impl.model;

import org.pentaho.metaverse.api.model.IArtifactMetadata;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.IParamInfo;
import org.pentaho.metaverse.api.model.IUserMetadata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

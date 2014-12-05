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

package com.pentaho.metaverse.api.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo( use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class" )
public interface IExecutionData {

  @JsonProperty( "startTime" )
  public Date getStartTime();

  public void setStartTime( Date timestamp );

  @JsonProperty( "endTime" )
  public Date getEndTime();

  public void setEndTime( Date timestamp );

  @JsonProperty( "failureCount" )
  public long getFailureCount();

  public void setFailureCount( long failureCount );

  @JsonProperty( "executorServer" )
  public String getExecutorServer();

  public void setExecutorServer( String executorServer );

  @JsonProperty( "executorUser" )
  public String getExecutorUser();

  public void setExecutorUser( String executorUser );

  @JsonProperty( "clientExecutor" )
  public String getClientExecutor();

  public void setClientExecutor( String clientExecutor );

  @JsonProperty( "loggingChannelId" )
  public String getLoggingChannelId();

  public void setLoggingChannelId( String loggingChannelId );

  @JsonProperty( "parameters" )
  public List<IParamInfo<String>> getParameters();

  public void setParameters( List<IParamInfo<String>> parameters );

  public void addParameter( IParamInfo<String> parameter );

  @JsonProperty( "externalResources" )
  public Map<String, List<IExternalResourceInfo>> getExternalResources();

  public void setExternalResources( Map<String, List<IExternalResourceInfo>> externalResources );

  public void addExternalResource( String consumerName, IExternalResourceInfo externalResource );

  @JsonProperty( "variables" )
  public Map<Object, Object> getVariables();

  public void addVariable( String name, String value );

  @JsonProperty( "arguments" )
  public List<Object> getArguments();

  public void putArgument( int index, Object value );

  @JsonProperty( "artifactMeta" )
  public IArtifactMetadata getArtifactMetadata();

  public void setArtifactMetadata( IArtifactMetadata artifactMetadata );

  @JsonProperty( "userMeta" )
  public IUserMetadata getUserMetadata();

  public void setUserMetadata( IUserMetadata userMetadata );

}

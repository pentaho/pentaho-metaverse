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

package org.pentaho.metaverse.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonTypeInfo( use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = IInfo.JSON_PROPERTY_CLASS )
public interface IExecutionData {
  public static final String JSON_PROPERTY_START_TIME = "startTime";
  public static final String JSON_PROPERTY_END_TIME = "endTime";
  public static final String JSON_PROPERTY_FAILURE_COUNT = "failureCount";
  public static final String JSON_PROPERTY_EXECUTOR_SERVER = "executorServer";
  public static final String JSON_PROPERTY_EXECUTOR_USER = "executorUser";
  public static final String JSON_PROPERTY_CLIENT_EXECUTOR = "clientExecutor";
  public static final String JSON_PROPERTY_LOGGING_CHANNEL_ID = "loggingChannelId";
  public static final String JSON_PROPERTY_PARAMETERS = "parameters";
  public static final String JSON_PROPERTY_EXTERNAL_RESOURCES = "externalResources";
  public static final String JSON_PROPERTY_VARIABLES = "variables";
  public static final String JSON_PROPERTY_ARGUMENTS = "arguments";
  public static final String JSON_PROPERTY_ARTIFACT_META = "artifactMeta";
  public static final String JSON_PROPERTY_USER_META = "userMeta";

  @JsonProperty( JSON_PROPERTY_START_TIME )
  public Date getStartTime();

  public void setStartTime( Date timestamp );

  @JsonProperty( JSON_PROPERTY_END_TIME )
  public Date getEndTime();

  public void setEndTime( Date timestamp );

  @JsonProperty( JSON_PROPERTY_FAILURE_COUNT )
  public long getFailureCount();

  public void setFailureCount( long failureCount );

  @JsonProperty( JSON_PROPERTY_EXECUTOR_SERVER )
  public String getExecutorServer();

  public void setExecutorServer( String executorServer );

  @JsonProperty( JSON_PROPERTY_EXECUTOR_USER )
  public String getExecutorUser();

  public void setExecutorUser( String executorUser );

  @JsonProperty( JSON_PROPERTY_CLIENT_EXECUTOR )
  public String getClientExecutor();

  public void setClientExecutor( String clientExecutor );

  @JsonProperty( JSON_PROPERTY_LOGGING_CHANNEL_ID )
  public String getLoggingChannelId();

  public void setLoggingChannelId( String loggingChannelId );

  @JsonProperty( JSON_PROPERTY_PARAMETERS )
  public List<IParamInfo<String>> getParameters();

  public void setParameters( List<IParamInfo<String>> parameters );

  public void addParameter( IParamInfo<String> parameter );

  @JsonProperty( JSON_PROPERTY_EXTERNAL_RESOURCES )
  public Map<String, List<IExternalResourceInfo>> getExternalResources();

  public void setExternalResources( Map<String, List<IExternalResourceInfo>> externalResources );

  public void addExternalResource( String consumerName, IExternalResourceInfo externalResource );

  @JsonProperty( JSON_PROPERTY_VARIABLES )
  public Map<Object, Object> getVariables();

  public void addVariable( String name, String value );

  @JsonProperty( JSON_PROPERTY_ARGUMENTS )
  public List<Object> getArguments();

  public void putArgument( int index, Object value );

  @JsonProperty( JSON_PROPERTY_ARTIFACT_META )
  public IArtifactMetadata getArtifactMetadata();

  public void setArtifactMetadata( IArtifactMetadata artifactMetadata );

  @JsonProperty( JSON_PROPERTY_USER_META )
  public IUserMetadata getUserMetadata();

  public void setUserMetadata( IUserMetadata userMetadata );

}

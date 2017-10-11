/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

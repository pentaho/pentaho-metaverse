/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.frames;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import org.pentaho.dictionary.DictionaryConst;

/**
 * User: RFellows Date: 9/4/14
 */
public interface KettleNode extends Concept {
  @Property( DictionaryConst.PROPERTY_PATH )
  public String getPath();

  @Property( DictionaryConst.PROPERTY_ARTIFACT_VERSION )
  public String getVersion();

  @Property( "extendedDescription" )
  public String getExtendedDescription();

  @Property( DictionaryConst.PROPERTY_STATUS )
  public String getStatus();

  @Property( DictionaryConst.PROPERTY_LAST_MODIFIED )
  public String getLastModified();

  @Property( DictionaryConst.PROPERTY_LAST_MODIFIED_BY )
  public String getLastModifiedBy();

  @Property( DictionaryConst.PROPERTY_CREATED )
  public String getCreated();

  @Property( DictionaryConst.PROPERTY_CREATED_BY )
  public String getCreatedBy();

  @GremlinGroovy( "it.in('contains').has( 'type', T.eq, 'Locator' )" )
  public LocatorNode getLocator();

  @GremlinGroovy( value="it.property(paramKey).collect()[0]", frame=false )
  public String getParameter( @GremlinParam( "paramKey" ) String paramKey );

}

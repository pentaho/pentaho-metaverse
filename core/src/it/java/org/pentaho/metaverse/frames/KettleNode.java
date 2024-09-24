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

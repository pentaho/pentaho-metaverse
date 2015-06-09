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

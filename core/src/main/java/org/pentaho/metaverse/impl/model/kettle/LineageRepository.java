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

package org.pentaho.metaverse.impl.model.kettle;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: RFellows Date: 11/19/14
 */
public class LineageRepository extends SimpleRepository {

  Map<ObjectId, Map<String, Object>> stepAttributeCache;
  Map<ObjectId, List<Map<String, Object>>> stepFieldCache;

  Map<ObjectId, Map<String, Object>> jobEntryAttributeCache;
  Map<ObjectId, List<Map<String, Object>>> jobEntryFieldCache;

  public LineageRepository() {
    stepAttributeCache = new HashMap<ObjectId, Map<String, Object>>();
    stepFieldCache = new HashMap<ObjectId, List<Map<String, Object>>>();
    jobEntryAttributeCache = new HashMap<ObjectId, Map<String, Object>>();
    jobEntryFieldCache = new HashMap<ObjectId, List<Map<String, Object>>>();
  }

  @Override public boolean getStepAttributeBoolean( ObjectId id_step, int nr, String code, boolean def )
    throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    Boolean attr = (Boolean) attrs.get( code );
    if ( attr != null ) {
      return attr;
    } else {
      return def;
    }
  }

  @Override public boolean getStepAttributeBoolean( ObjectId id_step, int nr, String code ) throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    Boolean attr = (Boolean) attrs.get( code );
    if ( attr != null ) {
      return attr;
    } else {
      return false;
    }
  }

  @Override public boolean getStepAttributeBoolean( ObjectId id_step, String code ) throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    Boolean attr = (Boolean) attrs.get( code );
    return attr == null ? false : attr;
  }

  @Override public long getStepAttributeInteger( ObjectId id_step, int nr, String code ) throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    Number attr = (Number) attrs.get( code );
    return attr == null ? 0L : attr.longValue();
  }

  @Override public long getStepAttributeInteger( ObjectId id_step, String code ) throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    Number attr = (Number) attrs.get( code );
    return attr == null ? 0L : attr.longValue();
  }

  @Override public String getStepAttributeString( ObjectId id_step, int nr, String code ) throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    Object attr = attrs.get( code );
    return attr == null ? null : attr.toString();
  }

  @Override public String getStepAttributeString( ObjectId id_step, String code ) throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    Object attr = attrs.get( code );
    return attr == null ? null : attr.toString();
  }

  public Map<String, Object> getStepAttributesCache( ObjectId id_step ) {
    Map<String, Object> attrs = stepAttributeCache.get( id_step );
    if ( attrs == null ) {
      attrs = new TreeMap<String, Object>();
      stepAttributeCache.put( id_step, attrs );
    }
    return attrs;
  }

  public List<Map<String, Object>> getStepFieldsCache( ObjectId id_step ) {
    List<Map<String, Object>> fieldList = stepFieldCache.get( id_step );
    if ( fieldList == null ) {
      fieldList = new ArrayList<Map<String, Object>>( 100 );
      stepFieldCache.put( id_step, fieldList );
    }
    return fieldList;
  }

  public Map<String, Object> getStepFieldAttributesCache( ObjectId id_step, int number ) {
    List<Map<String, Object>> fieldList = getStepFieldsCache( id_step );
    if ( number + 1 > fieldList.size() ) {
      for ( int i = fieldList.size(); i < number + 1; i++ ) {
        fieldList.add( i, new TreeMap<String, Object>() );
      }
    }
    Map<String, Object> fieldAttrs = fieldList.get( number );
    if ( fieldAttrs == null ) {
      fieldAttrs = new TreeMap<String, Object>();
      fieldList.add( number, fieldAttrs );
    }
    return fieldAttrs;
  }

  public Map<String, Object> getJobEntryAttributesCache( ObjectId id_jobentry ) {
    Map<String, Object> attrs = jobEntryAttributeCache.get( id_jobentry );
    if ( attrs == null ) {
      attrs = new TreeMap<String, Object>();
      jobEntryAttributeCache.put( id_jobentry, attrs );
    }
    return attrs;
  }

  public Map<String, Object> getJobEntryFieldAttributesCache( ObjectId id_jobentry, int number ) {
    List<Map<String, Object>> fieldList = getJobEntryFieldsCache( id_jobentry );
    if ( number + 1 > fieldList.size() ) {
      for ( int i = fieldList.size(); i < number + 1; i++ ) {
        fieldList.add( i, new TreeMap<String, Object>() );
      }
    }
    Map<String, Object> fieldAttrs = fieldList.get( number );
    if ( fieldAttrs == null ) {
      fieldAttrs = new TreeMap<String, Object>();
      fieldList.add( number, fieldAttrs );
    }
    return fieldAttrs;
  }

  public List<Map<String, Object>> getJobEntryFieldsCache( ObjectId id_jobentry ) {
    List<Map<String, Object>> fieldList = jobEntryFieldCache.get( id_jobentry );
    if ( fieldList == null ) {
      fieldList = new ArrayList<Map<String, Object>>( 100 );
      jobEntryFieldCache.put( id_jobentry, fieldList );
    }
    return fieldList;
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code,
                                           String value ) throws KettleException {

    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    attrs.put( code, value );

  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, String value )
    throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    attrs.put( code, value );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code,
                                           boolean value ) throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    attrs.put( code, value );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, boolean value )
    throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    attrs.put( code, value );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code,
                                           long value ) throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    attrs.put( code, value );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, long value )
    throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    attrs.put( code, value );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code,
                                           double value ) throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    attrs.put( code, value );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, double value )
    throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    attrs.put( code, value );
  }

  @Override public int countNrStepAttributes( ObjectId id_step, String code ) throws KettleException {
    int number = 0;

    List<Map<String, Object>> fieldList = getStepFieldsCache( id_step );

    for ( int i = 0; i < fieldList.size(); i++ ) {
      Map<String, Object> fieldAttrs = fieldList.get( i );
      if ( fieldAttrs.containsKey( code ) ) {
        number++;
      }
    }

    return number;
  }

  @Override public DatabaseMeta loadDatabaseMetaFromStepAttribute( ObjectId id_step, String code,
    List<DatabaseMeta> databases ) throws KettleException {
    DatabaseMeta db = null;
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    for ( DatabaseMeta database : databases ) {
      Object dbname = attrs.get( code );
      if ( database.getName().equals( dbname ) ) {
        db = database;
        break;
      }
    }
    return db;
  }

  @Override public void saveDatabaseMetaStepAttribute( ObjectId id_transformation, ObjectId id_step, String code,
    DatabaseMeta database ) throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    // just save that database name as a reference here
    if ( database != null ) {
      attrs.put( code, database.getName() );
    }
  }

  @Override public void insertStepDatabase( ObjectId id_transformation, ObjectId id_step, ObjectId id_database )
    throws KettleException {
    // do nothing
  }

  @Override
  public void saveConditionStepAttribute( ObjectId id_transformation, ObjectId id_step, String code,
    Condition condition ) throws KettleException {
    // TODO
  }

  @Override
  public Condition loadConditionFromStepAttribute( ObjectId id_step, String code ) throws KettleException {
    // TODO
    return new Condition();
  }

  @Override
  public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String code, String value )
    throws KettleException {
    Map<String, Object> attrs = getJobEntryFieldAttributesCache( id_jobentry, nr );
    attrs.put( code, value );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String code, String value )
    throws KettleException {
    Map<String, Object> attrs = getJobEntryAttributesCache( id_jobentry );
    attrs.put( code, value );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String code, boolean value )
    throws KettleException {
    Map<String, Object> attrs = getJobEntryFieldAttributesCache( id_jobentry, nr );
    attrs.put( code, value );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String code, boolean value )
    throws KettleException {
    Map<String, Object> attrs = getJobEntryAttributesCache( id_jobentry );
    attrs.put( code, value );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String code, long value )
    throws KettleException {
    Map<String, Object> attrs = getJobEntryFieldAttributesCache( id_jobentry, nr );
    attrs.put( code, value );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String code, long value )
    throws KettleException {
    Map<String, Object> attrs = getJobEntryAttributesCache( id_jobentry );
    attrs.put( code, value );
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, String code ) throws KettleException {
    Map<String, Object> attrs = getJobEntryAttributesCache( id_jobentry );
    Boolean attr = (Boolean) attrs.get( code );
    return attr == null ? null : attr;
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, int nr, String code ) throws KettleException {
    Map<String, Object> attrs = getJobEntryFieldAttributesCache( id_jobentry, nr );
    Boolean attr = (Boolean) attrs.get( code );
    return attr == null ? false : attr;
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, String code, boolean def ) throws KettleException {
    Map<String, Object> attrs = getJobEntryAttributesCache( id_jobentry );
    Boolean attr = (Boolean) attrs.get( code );
    return attr == null ? def : attr;
  }

  @Override
  public long getJobEntryAttributeInteger( ObjectId id_jobentry, String code ) throws KettleException {
    Map<String, Object> attrs = getJobEntryAttributesCache( id_jobentry );
    Number attr = (Number) attrs.get( code );
    return attr == null ? 0L : attr.longValue();
  }

  @Override
  public long getJobEntryAttributeInteger( ObjectId id_jobentry, int nr, String code ) throws KettleException {
    Map<String, Object> attrs = getJobEntryFieldAttributesCache( id_jobentry, nr );
    Number attr = (Number) attrs.get( code );
    return attr == null ? 0L : attr.longValue();
  }

  @Override
  public String getJobEntryAttributeString( ObjectId id_jobentry, String code ) throws KettleException {
    Map<String, Object> attrs = getJobEntryAttributesCache( id_jobentry );
    return (String) attrs.get( code );
  }

  @Override
  public String getJobEntryAttributeString( ObjectId id_jobentry, int nr, String code ) throws KettleException {
    Map<String, Object> attrs = getJobEntryFieldAttributesCache( id_jobentry, nr );
    return (String) attrs.get( code );
  }
}


/*!
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

package org.pentaho.metaverse.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * BasePropertiesHolder provides methods for getting and setting key/value pairs (i.e. properties)
 */
public class PropertiesHolder implements IHasProperties {

  protected Map<String, Object> properties;
  private boolean dirty = false;

  public PropertiesHolder() {
    properties = new HashMap<String, Object>();
  }

  /**
   * Gets the property value for the specified key.
   *
   * @param key the lookup key
   * @return the value object for the property, or null if none is found
   */
  @Override
  public Object getProperty( String key ) {
    return properties.get( key );
  }

  /**
   * Gets the property value (as a String) for the specified key.
   *
   * @param key the lookup key
   * @return the string value object for the property, or null if none is found
   */
  public String getPropertyAsString( String key ) {
    Object prop = getProperty( key );
    if ( prop == null ) {
      return null;
    } else {
      return prop.toString();
    }
  }

  /**
   * Sets a value for the property with the given key.
   *
   * @param key   the property name for which to set the given value
   * @param value the value to assign to the property key
   */
  @Override
  public void setProperty( String key, Object value ) {
    dirty = true;
    properties.put( key, value );
  }

  /**
   * Removes and returns the value assigned to the property for the given key.
   *
   * @param key the key for which to remove the property's value
   * @return the value that was removed, or null if the key or value could not be found
   */
  @Override
  public Object removeProperty( String key ) {
    dirty = true;
    return properties.remove( key );
  }

  /**
   * Gets the set of keys (property names).
   *
   * @return a Set of property keys
   */
  @Override
  public Set<String> getPropertyKeys() {
    return properties.keySet();
  }

  /**
   * Returns the properties as a key/value Map.
   *
   * @return the property key/value assignments
   */
  @Override public Map<String, Object> getProperties() {
    return properties;
  }

  /**
   * Sets the given property keys to the given property values.
   *
   * @param props
   */
  @Override
  public void setProperties( Map<String, Object> props ) {
    dirty = true;
    properties.putAll( props );
  }

  /**
   * Removes the values assigned to the given property keys.
   *
   * @param keys
   */
  @Override
  public void removeProperties( Set<String> keys ) {
    if ( keys != null ) {
      dirty = true;
      for ( String key : keys ) {
        properties.remove( key );
      }
    }
  }

  /**
   * Removes all properties (key/value assignments).
   */
  @Override public void clearProperties() {
    dirty = true;
    properties.clear();
  }

  /**
   * Checks to see if the key has been assigned.
   *
   * @param key the String key to check
   * @return true if the key has been assigned a value, false otherwise
   */
  @Override
  public boolean containsKey( String key ) {
    if ( properties == null ) {
      return false;
    } else {
      return properties.containsKey( key );
    }
  }

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public void setDirty( boolean dirty ) {
    this.dirty = dirty;
  }

  @Override
  public String toString() {
    if ( properties == null ) {
      return super.toString();
    }

    return properties.toString();
  }

}

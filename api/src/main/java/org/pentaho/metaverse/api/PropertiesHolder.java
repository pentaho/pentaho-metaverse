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

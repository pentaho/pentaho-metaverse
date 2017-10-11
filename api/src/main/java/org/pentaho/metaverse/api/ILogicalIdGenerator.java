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

package org.pentaho.metaverse.api;

import java.util.Set;

public interface ILogicalIdGenerator {

  /**
   * Sets the property names that should be used in generating a logical id.
   *
   * @param propertyKeys property keys that indicate what makes this node logically unique
   */
  public void setLogicalIdPropertyKeys( String... propertyKeys );

  /**
   *
   * @return the Set of property keys that define logical equality for like nodes
   */
  public Set<String> getLogicalIdPropertyKeys();

  /**
   * Generates an ID based on properties that make it unique. It also will set that ID as a property on the node passed
   * in named 'logicalId'. It should reliably generate the same id for 2 nodes that are logically equal
   *
   * @param propertiesNode the object requiring a logical id
   * @return the logicalId generated
   */
  public String generateId( IHasProperties propertiesNode );

}

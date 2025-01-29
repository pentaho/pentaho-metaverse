/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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

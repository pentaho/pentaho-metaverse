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

package org.pentaho.metaverse.api;

/**
 * The IRequiresMetaverseBuilder contains helper methods for interfaces and classes that need a metaverse builder
 */
public interface IRequiresMetaverseBuilder {

  /**
   * Sets the metaverse builder used by analyzer(s) to create nodes and links in the metaverse
   * of content.
   *
   * @param builder the metaverse builder
   */
  void setMetaverseBuilder( IMetaverseBuilder builder );

  /**
   * Returns the metaverse builder used by analyzer(s) to create nodes and links in the metaverse
   * @return
   */
  IMetaverseBuilder getMetaverseBuilder();
}

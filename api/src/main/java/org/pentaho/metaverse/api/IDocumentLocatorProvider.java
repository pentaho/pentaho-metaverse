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

import java.util.Set;

/**
 * Provides IDocumentLocators to the Metaverse
 */
public interface IDocumentLocatorProvider {

  /**
   * Returns all known DocumentLocators available in the metaverse
   * @return a Set of IDocumentLocators
   */
  Set<IDocumentLocator> getDocumentLocators();

  /**
   * Adds a locator
   * @param documentLocator IDocumentLocator to add
   */
  void addDocumentLocator( IDocumentLocator documentLocator );

  /**
   * Removes a locator
   * @param documentLocator IDocumentLocator to remove
   * @return success or failure (true or false)
   */
  boolean removeDocumentLocator( IDocumentLocator documentLocator );

}

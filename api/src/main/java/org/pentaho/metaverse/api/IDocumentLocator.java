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

import java.net.URI;

/**
 * Discovers documents intended to be scanned for Metaverse-relevant content
 */
public interface IDocumentLocator extends IRequiresMetaverseBuilder {

  /**
   * Starts the scanning procedure used by this document locator
   * @throws MetaverseLocatorException if
   * scan cannot be executed
   */
  void startScan() throws MetaverseLocatorException;

  /**
   * Stops the scanning procedure used by this document locator
   */
  void stopScan();

  /**
   * Adds to the locator a listener for document events (document found, created, deleted, etc.)
   */
  void addDocumentListener( IDocumentListener listener );

  /**
   * Removes the specified listener from this locator
   *
   * @param listener the document listener to remove
   */
  void removeDocumentListener( IDocumentListener listener );

  /**
   * Notify listeners of a document event
   *
   * @param event the document event to report
   */
  void notifyListeners( IDocumentEvent event );

  /**
   * Get the root location that this IDocumentLocator is responsible for
   * @return URI root location
   */
  URI getRootUri();

}

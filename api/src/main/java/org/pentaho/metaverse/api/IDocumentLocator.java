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

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

/**
 * Listens for events fired from IDocumentLocator objects
 */
public interface IDocumentListener {
  /**
   * Called by when a new IDocument is discovered
   * @param event
   */
  void onEvent( IDocumentEvent event );
}

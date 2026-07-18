/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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

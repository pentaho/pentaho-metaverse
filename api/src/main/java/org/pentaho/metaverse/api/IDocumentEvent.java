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
 * The IDocumentEvent interface represents events that occur when metaverse documents are being processed,
 * such as create, read, update, delete.
 */
public interface IDocumentEvent {

  /**
   * Gets the document to which this event occurred.
   *
   * @return the IDocument instance to which this event occurred.
   */
  IDocument getDocument();

  /**
   * Gets the type of this document event
   *
   * @return the event type
   */
  String getEventType();

}

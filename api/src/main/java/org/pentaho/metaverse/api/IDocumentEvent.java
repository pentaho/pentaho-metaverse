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

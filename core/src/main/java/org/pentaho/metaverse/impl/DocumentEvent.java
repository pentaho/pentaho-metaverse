/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.impl;

import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentEvent;

/**
 * Simple implementation of @see IDocumentEvent
 * @author jdixon
 *
 */
public class DocumentEvent implements IDocumentEvent {

  private IDocument document;

  private String type;

  @Override
  public IDocument getDocument() {
    return document;
  }

  public void setDocument( IDocument document ) {
    this.document = document;
  }

  @Override
  public String getEventType() {
    return type;
  }

  public void setEventType( String type ) {
    this.type = type;
  }

}

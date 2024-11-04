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

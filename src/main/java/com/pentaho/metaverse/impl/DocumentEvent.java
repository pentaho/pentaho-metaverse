package com.pentaho.metaverse.impl;

import org.pentaho.platform.api.metaverse.IDocumentEvent;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;

/**
 * Simple implementation of @see org.pentaho.platform.api.metaverse.IDocumentEvent
 * @author jdixon
 *
 */
public class DocumentEvent implements IDocumentEvent {

  private IMetaverseDocument document;
  
  private String type;
  
  @Override
  public IMetaverseDocument getDocument() {
    return document;
  }

  public void setDocument( IMetaverseDocument document ) {
    this.document = document;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType( String type ) {
    this.type = type;
  }
  
}

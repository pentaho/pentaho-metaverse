package com.pentaho.metaverse.impl;

import org.pentaho.platform.api.metaverse.IMetaverseDocument;

public class MetaverseDocument implements IMetaverseDocument {

  String name;
  String id;
  String type;
  Object content;
  
  public void setID( String id ) {
    this.id = id;
  }

  public Object getContent() {
    return content;
  }

  public void setContent( Object content ) {
    this.content = content;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public void setType( String type ) {
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getID() {
    return id;
  }

  @Override
  public String getType() {
    return type;
  }

}

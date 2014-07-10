package com.pentaho.metaverse.locator;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.metaverse.IDocumentEvent;
import org.pentaho.platform.api.metaverse.IDocumentListener;
import org.pentaho.platform.api.metaverse.IDocumentLocator;
import org.pentaho.platform.engine.core.system.PentahoBase;

import com.pentaho.metaverse.api.IIdGenerator;

public abstract class BaseLocator extends PentahoBase implements IDocumentLocator, IIdGenerator {

  private static final long serialVersionUID = 693428630030858039L;

  protected IPentahoSession session;
  
  protected String id = "";
  
  protected String indexerType;
  
  private List<IDocumentListener> listeners = new ArrayList<IDocumentListener>();
  
  @Override
  public void addDocumentListener( IDocumentListener listener ) {
    listeners.add( listener );
  }

  @Override
  public void notifyListeners( IDocumentEvent event ) {
    for( IDocumentListener listener : listeners ) {
      listener.onEvent( event );
    }
  }

  @Override
  public void removeDocumentListener( IDocumentListener listener ) {
    listeners.remove( listener );
  }
  
  public BaseLocator() {
  }

  public String getRepositoryId() {
    return id;
  }

  public void setRepositoryId( String id ) {
    this.id = id;
  }

  public String getIndexerType() {
    return indexerType;
  }

  public void setIndexerType( String indexerType ) {
    this.indexerType = indexerType;
  }
  
}

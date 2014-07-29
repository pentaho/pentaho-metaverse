package com.pentaho.metaverse.locator;

import org.pentaho.platform.api.metaverse.IDocumentListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestFileSystemLocator extends FileSystemLocator {

  private static final long serialVersionUID = 5274942564530788651L;

  public static long delay = 0;

  public TestFileSystemLocator() {
    super();
  }

  public TestFileSystemLocator( List<IDocumentListener> documentListeners ) {
    super( documentListeners );
  }

  @Override
  protected Object getFileContents( File file, String type ) throws Exception {
    if ( delay != 0 ) {
      try {
        Thread.sleep( delay );
      } catch ( InterruptedException e ) {
      }
    }
    return super.getFileContents( file, type );
  }

}

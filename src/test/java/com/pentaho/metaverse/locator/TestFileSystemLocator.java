package com.pentaho.metaverse.locator;

import java.io.File;

public class TestFileSystemLocator extends FileSystemLocator {

  private static final long serialVersionUID = 5274942564530788651L;

  public static long delay = 0;

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

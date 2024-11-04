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


package org.pentaho.metaverse.locator;

import org.pentaho.metaverse.api.IDocumentListener;

import java.io.File;
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
  protected Object getContents( File file ) throws Exception {
    if ( delay != 0 ) {
      try {
        Thread.sleep( delay );
      } catch ( InterruptedException e ) {
      }
    }
    return super.getContents( file );
  }

}

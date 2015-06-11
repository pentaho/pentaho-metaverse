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

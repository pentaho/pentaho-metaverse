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

package org.pentaho.metaverse.api.analyzer.kettle;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.metaverse.api.MetaverseException;

import java.io.File;

public class KettleAnalyzerUtil {

  /**
   * Utility method for normalizing file paths used in Metaverse Id generation. It will convert a valid path
   * into a consistent path regardless of URI notation or filesystem absolute path.
   *
   * @param filePath full path to normalize
   * @return the normalized path
   */
  public static String normalizeFilePath( String filePath ) throws MetaverseException {
    try {
      FileObject fo = KettleVFS.getFileObject( filePath );
      String path = fo.getURL().getPath();
      File f = new File( path );
      return f.getAbsolutePath();
    } catch ( Exception e ) {
      throw new MetaverseException( e );
    }
  }

}

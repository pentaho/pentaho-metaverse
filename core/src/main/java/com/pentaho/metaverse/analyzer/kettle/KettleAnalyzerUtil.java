/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.metaverse.analyzer.kettle;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.platform.api.metaverse.MetaverseException;

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

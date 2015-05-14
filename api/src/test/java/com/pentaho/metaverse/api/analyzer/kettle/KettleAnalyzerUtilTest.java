/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.api.analyzer.kettle;

import org.apache.commons.vfs.FileObject;
import org.junit.Test;
import org.pentaho.di.core.vfs.KettleVFS;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * User: RFellows Date: 8/14/14
 */
public class KettleAnalyzerUtilTest {

  @Test
  public void testDefaultConstructor() {
    assertNotNull( new KettleAnalyzerUtil() );
  }

  @Test
  public void testNormalizeFilePath() throws Exception {
    String input;
    String expected;
    try {
      File f = File.createTempFile( "This is a text file", ".txt" );
      input = f.getAbsolutePath();
      expected = f.getAbsolutePath();
    } catch ( IOException ioe ) {
      // If this didn't work, we're running on a system where we can't create files, like CI perhaps.
      // In that case, use a RAM file. This test doesn't do much in that case, but it will pass.
      FileObject f = KettleVFS.createTempFile( "This is a text file", ".txt", "ram://" );
      input = f.getName().getPath();
      expected = f.getName().getPath();
    }

    String result = KettleAnalyzerUtil.normalizeFilePath( input );
    assertEquals( expected, result );

  }
}

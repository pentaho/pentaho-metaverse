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


package org.pentaho.metaverse.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.metaverse.api.ILineageCollector;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class LineageStreamingOutputTest {

  @Mock
  ILineageCollector collector;
  @Mock
  OutputStream outputStream;

  @Test
  public void testWrite() throws Exception {
    List<String> artifacts = new ArrayList<>();
    LineageStreamingOutput stream = new LineageStreamingOutput( artifacts, collector );

    stream.write( outputStream );

    verify( collector ).compressArtifacts( eq( artifacts ), eq( outputStream ) );
    verify( outputStream ).flush();
  }
}

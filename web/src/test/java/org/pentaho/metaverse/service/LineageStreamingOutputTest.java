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


package org.pentaho.metaverse.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.metaverse.api.ILineageCollector;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith( MockitoJUnitRunner.class )
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

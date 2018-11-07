/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.api.model.BaseMetaverseBuilder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * User: RFellows Date: 8/14/14
 */
@RunWith( PowerMockRunner.class )
@PrepareForTest( KettleVFS.class )
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

  @Test
  public void testNormalizeFilePathSafely() throws Exception {

    final String path = "temp/foo";
    assertNotEquals( "temp/foo", KettleAnalyzerUtil.normalizeFilePathSafely( path ) );
    assertTrue( KettleAnalyzerUtil.normalizeFilePathSafely( path ).endsWith( "temp" + File.separator + "foo" ) );

    // verify that when an exception is thrown, the original value is returned
    PowerMockito.mockStatic( KettleVFS.class );
    Mockito.when( KettleVFS.getFileObject( path ) ).thenThrow( new KettleFileException( "mockedException" ) );
    assertEquals( "temp/foo", KettleAnalyzerUtil.normalizeFilePathSafely( path ) );
  }

  @Test
  public void testGetFileName() {
    assertNull( KettleAnalyzerUtil.getFilename( null ) );

    final TransMeta transMeta = Mockito.mock( TransMeta.class );
    assertNull( KettleAnalyzerUtil.getFilename( transMeta ) );

    Mockito.doReturn( "my_file_name" ).when( transMeta ).getFilename();
    assertEquals( "my_file_name", KettleAnalyzerUtil.getFilename( transMeta ) );

    Mockito.doReturn( "path_and_file_name" ).when( transMeta ).getPathAndName();
    // filename is still set and will be returned
    assertEquals( "my_file_name", KettleAnalyzerUtil.getFilename( transMeta ) );
    // when filename is null, pathAndFilename is returned
    Mockito.doReturn( null ).when( transMeta ).getFilename();
    assertEquals( "path_and_file_name", KettleAnalyzerUtil.getFilename( transMeta ) );

    Mockito.doReturn( "my_ext" ).when( transMeta ).getDefaultExtension();
    assertEquals( "path_and_file_name.my_ext", KettleAnalyzerUtil.getFilename( transMeta ) );
  }

  @Test
  public void tesBuildDocument() throws MetaverseException {
    final IMetaverseBuilder builder = new BaseMetaverseBuilder( null );
    final AbstractMeta transMeta = Mockito.mock( TransMeta.class );
    final String transName = "MyTransMeta";
    Mockito.doReturn( transName ).when( transMeta ).getName();
    Mockito.doReturn( "ktr" ).when( transMeta ).getDefaultExtension();
    final String id = "path.ktr";
    final String namespaceId = "MyNamespace";
    final INamespace namespace = new Namespace( namespaceId );

    assertNull( KettleAnalyzerUtil.buildDocument( null, transMeta, id, namespace ) );

    IDocument document = KettleAnalyzerUtil.buildDocument( builder, transMeta, id, namespace );
    assertNotNull( document );
    assertEquals( namespace, document.getNamespace() );
    assertEquals( transMeta, document.getContent() );
    assertEquals( id, document.getStringID() );
    assertEquals( transName, document.getName() );
    assertEquals( "ktr", document.getExtension() );
    assertEquals( DictionaryConst.CONTEXT_RUNTIME, document.getContext().getContextName() );
    assertEquals( document.getName(), document.getProperty( DictionaryConst.PROPERTY_NAME ) );
    assertEquals( KettleAnalyzerUtil.normalizeFilePath( "path.ktr" ), document.getProperty( DictionaryConst
      .PROPERTY_PATH ) );
    assertEquals(namespaceId, document.getProperty( DictionaryConst.PROPERTY_NAMESPACE ) );
  }
}

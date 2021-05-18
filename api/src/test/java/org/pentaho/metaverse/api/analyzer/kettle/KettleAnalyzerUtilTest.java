/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.ISubTransAwareMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputStep;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.api.model.BaseMetaverseBuilder;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
    assertEquals( namespaceId, document.getProperty( DictionaryConst.PROPERTY_NAMESPACE ) );
  }

  @Test
  public void testGetSubTransMetaPath() throws MetaverseAnalyzerException {

    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( null, null ) );

    final ISubTransAwareMeta meta = Mockito.mock( ISubTransAwareMeta.class );
    final TransMeta subTransMeta = Mockito.mock( TransMeta.class );
    final StepMeta parentStepMeta = Mockito.mock( StepMeta.class );
    final TransMeta parentTransMeta = Mockito.mock( TransMeta.class );

    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( meta, null ) );
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( null, subTransMeta ) );
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ) );

    Mockito.doReturn( ObjectLocationSpecificationMethod.FILENAME ).when( meta ).getSpecificationMethod();
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( meta, null ) );
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( null, subTransMeta ) );
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ) );

    Mockito.doReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME ).when( meta ).getSpecificationMethod();
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( meta, null ) );
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( null, subTransMeta ) );
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ) );

    Mockito.doReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE ).when( meta ).getSpecificationMethod();
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( meta, null ) );
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( null, subTransMeta ) );
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ) );

    Mockito.doReturn( ObjectLocationSpecificationMethod.FILENAME ).when( meta ).getSpecificationMethod();
    Mockito.doReturn( parentStepMeta ).when( meta ).getParentStepMeta();
    Mockito.doReturn( "foo" ).when( meta ).getFileName();
    assertTrue( KettleAnalyzerUtil.getSubTransMetaPath( meta, null ).endsWith( File.separator + "foo" ) );
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( null, subTransMeta ) );
    assertTrue( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ).endsWith( File.separator + "foo" ) );

    Mockito.doReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME ).when( meta ).getSpecificationMethod();
    assertTrue( KettleAnalyzerUtil.getSubTransMetaPath( meta, null ).endsWith( File.separator + "foo" ) );
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( null, subTransMeta ) );
    assertTrue( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ).endsWith( File.separator + "foo" ) );

    Mockito.doReturn( "dir/foe" ).when( subTransMeta ).getPathAndName();
    assertTrue( KettleAnalyzerUtil.getSubTransMetaPath( meta, null ).endsWith( File.separator + "foo" ) );
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( null, subTransMeta ) );
    assertTrue( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ).endsWith(
      File.separator + "dir" + File.separator + "foe" ) );

    Mockito.doReturn( "ktr" ).when( subTransMeta ).getDefaultExtension();
    assertTrue( KettleAnalyzerUtil.getSubTransMetaPath( meta, null ).endsWith( File.separator + "foo" ) );
    assertNull( KettleAnalyzerUtil.getSubTransMetaPath( null, subTransMeta ) );
    assertTrue( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ).endsWith(
      File.separator + "dir" + File.separator + "foe.ktr" ) );

    Mockito.doReturn( "${rootDir}/dir/foe" ).when( subTransMeta ).getPathAndName();
    assertTrue( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ).endsWith(
      File.separator + "${rootDir}" + File.separator + "dir" + File.separator + "foe.ktr" ) );

    // mimic variable replacement where the variable is missing, should be removed from results
    Mockito.doReturn( parentTransMeta ).when( parentStepMeta ).getParentTransMeta();
    Mockito.doReturn( "/dir/foe.ktr" ).when( parentTransMeta ).environmentSubstitute( Mockito.anyString()  );
    assertTrue( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ).endsWith(
      File.separator + "dir" + File.separator + "foe.ktr" ) );
    assertFalse( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ).endsWith(
      File.separator + "${rootDir}" + File.separator + "dir" + File.separator + "foe.ktr" ) );

    // mimic variable replacement where the variable present
    Mockito.doReturn( "myRootDir/dir/foe.ktr"  ).when( parentTransMeta ).environmentSubstitute( Mockito.anyString()  );
    assertTrue( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ).endsWith(
      File.separator + "myRootDir" + File.separator + "dir" + File.separator + "foe.ktr" ) );
  }

  @Test
  public void testGetSubTransMeta() throws MetaverseAnalyzerException, KettleException {

    final ISubTransAwareMeta meta = Mockito.mock( ISubTransAwareMeta.class );
    final TransMeta subTransMeta = Mockito.mock( TransMeta.class );
    final StepMeta parentStepMeta = Mockito.mock( StepMeta.class );
    final TransMeta parentTransMeta = Mockito.mock( TransMeta.class );
    final Repository repo = Mockito.mock( Repository.class );
    final RepositoryDirectoryInterface repositoryDirectoryInterface = Mockito.mock( RepositoryDirectoryInterface.class );

    // test file in repository
    Mockito.doReturn( ObjectLocationSpecificationMethod.FILENAME ).when( meta ).getSpecificationMethod();
    Mockito.doReturn( parentStepMeta ).when( meta ).getParentStepMeta();
    Mockito.doReturn( parentTransMeta ).when( parentStepMeta ).getParentTransMeta();
    Mockito.doReturn( repo ).when( parentTransMeta ).getRepository();
    Mockito.doReturn( "/some/path/to/foo" ).when( meta ).getFileName();
    Mockito.doReturn( repositoryDirectoryInterface ).when( repo ).findDirectory( "/some/path/to" );
    Mockito.doReturn( subTransMeta ).when( repo ).loadTransformation( "foo", repositoryDirectoryInterface, null, true, null );
    Mockito.doReturn( "/some/path/to/foo" ).when( parentTransMeta ).environmentSubstitute( "/some/path/to/foo" );
    assertTrue( KettleAnalyzerUtil.getSubTransMeta( meta ).equals( subTransMeta ) );
  }

  @Mock
  private BaseFileInputMeta meta;

  @Mock
  private BaseFileInputMeta meta2;

  @Mock
  private TransMeta transMeta;

  private String path1 = "/path/to/file1";
  private String path1a = "/another/path/to/file1a";
  private String path2 = "/another/path/to/file2";
  private String sharedPath = "/shared/file";

  private String[] filePaths = { path1, path1a, sharedPath };
  private String[] filePaths2 = { path2, sharedPath };

  private StepMeta spyMeta;
  private StepMeta spyMeta2;

  private void initMetas() {

    when( transMeta.getFilename() ).thenReturn( "my_file" );

    spyMeta = spy( new StepMeta( "test", meta ) );
    when( meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( meta.writesToFile() ).thenReturn( true );
    when( meta.getFilePaths( false ) ).thenReturn( filePaths );

    spyMeta2 = spy( new StepMeta( "test2", meta2 ) );
    when( meta2.getParentStepMeta() ).thenReturn( spyMeta2 );
    when( spyMeta2.getParentTransMeta() ).thenReturn( transMeta );
    when( meta2.writesToFile() ).thenReturn( true );
    when( meta2.getFilePaths( false ) ).thenReturn( filePaths2 );
  }

  @Test
  public void test_getResourcedFromMeta() throws Exception {
    initMetas();
    when( meta.isAcceptingFilenames() ).thenReturn( false );
    Set<IExternalResourceInfo>
      resources = (Set<IExternalResourceInfo>) KettleAnalyzerUtil.getResourcesFromMeta( meta, filePaths );
    assertFalse( resources.isEmpty() );
    assertEquals( 3, resources.size() );

    Set<IExternalResourceInfo> resources2 = (Set) KettleAnalyzerUtil.getResourcesFromMeta( meta2, filePaths2 );
    assertFalse( resources2.isEmpty() );
    assertEquals( 2, resources2.size() );
  }

  @Test
  public void getResourcesFromRowFileContentTest() {
    String filename = "filename";
    BaseFileInputStep step = mock( BaseFileInputStep.class );
    RowMetaInterface rowMeta = mock( RowMetaInterface.class );
    Object[] row = new Object[]{};
    IExternalResourceInfo resourceInfo = initMocksForGetResourcesFromRowTest( filename, step );
    assertFalse( KettleAnalyzerUtil.getResourcesFromRow( step, rowMeta, row ).contains( resourceInfo ) );
  }

  @Test
  public void getResourcesFromRowFileWithSchemeTest() throws Exception {
    String filename = "file://filename";
    BaseFileInputStep step = mock( BaseFileInputStep.class );
    RowMetaInterface rowMeta = mock( RowMetaInterface.class );
    Object[] row = new Object[]{};
    IExternalResourceInfo resourceInfo = initMocksForGetResourcesFromRowTest( filename, step );
    assertTrue( KettleAnalyzerUtil.getResourcesFromRow( step, rowMeta, row ).contains( resourceInfo ) );
  }

  @Test
  public void getResourcesFromRowEmptyFileTest() throws Exception {
    String filename = "";
    BaseFileInputStep step = mock( BaseFileInputStep.class );
    RowMetaInterface rowMeta = mock( RowMetaInterface.class );
    Object[] row = new Object[]{};
    IExternalResourceInfo resourceInfo = initMocksForGetResourcesFromRowTest( filename, step );
    assertFalse( KettleAnalyzerUtil.getResourcesFromRow( step, rowMeta, row ).contains( resourceInfo ) );
  }

  @Test
  public void getResourcesFromRowFullPathFileTest() throws Exception {
    String filename = "/filefolder/filename";
    BaseFileInputStep step = mock( BaseFileInputStep.class );
    RowMetaInterface rowMeta = mock( RowMetaInterface.class );
    Object[] row = new Object[]{};
    IExternalResourceInfo resourceInfo = initMocksForGetResourcesFromRowTest( filename, step );
    assertTrue( KettleAnalyzerUtil.getResourcesFromRow( step, rowMeta, row ).contains( resourceInfo ) );
  }

  private IExternalResourceInfo initMocksForGetResourcesFromRowTest( String filename, BaseFileInputStep step ) {
    StepMeta stepMeta = mock( StepMeta.class );
    TransMeta transMeta = mock( TransMeta.class );
    FileObject fileObject = mock( FileObject.class );
    when( fileObject.getPublicURIString() ).thenReturn( filename );
    PowerMockito.stub( PowerMockito.method( KettleVFS.class, "getFileObject", String.class, VariableSpace.class ) ).toReturn( fileObject );
    when( step.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getParentStepMeta() ).thenReturn( stepMeta );
    when( stepMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( transMeta.getName() ).thenReturn( "transName" );
    when( stepMeta.getName() ).thenReturn( "stepName" );
    when( step.environmentSubstitute( anyString() ) ).thenReturn( filename );
    return ExternalResourceInfoFactory.createFileResource( fileObject, true );
  }
}

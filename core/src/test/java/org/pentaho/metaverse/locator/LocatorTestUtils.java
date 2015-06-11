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

import org.apache.commons.io.IOUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocatorTestUtils {

  private static final String SOLUTION_PATH = "src/test/resources/solution";
  public static long delay = 0;

  public static Repository getFakeDiRepository() {
    Repository diRepo = new FakePurRepository( LocatorTestUtils.getMockIUnifiedRepository() );
    return diRepo;
  }

  public static IUnifiedRepository getMockIUnifiedRepository() {
    IUnifiedRepository repo = mock( IUnifiedRepository.class );

    when( repo.getTree( any( RepositoryRequest.class ) ) )
        .thenAnswer( new Answer<RepositoryFileTree>() {
          @Override
          public RepositoryFileTree answer( InvocationOnMock invocationOnMock ) throws Throwable {
            Object[] args = invocationOnMock.getArguments();
            return getTree( (RepositoryRequest) args[0] );
          }
        } );

    return repo;
  }

  /**
   * ************ load job and trans methods for the mock diRepo ****************
   */
  private static JobMeta loadJob( ObjectId arg0, String arg1 ) throws KettleException {
    if ( delay != 0 ) {
      try {
        Thread.sleep( delay );
      } catch ( InterruptedException e ) {
      }
    }
    System.out.println( "loadJob " + arg0 );
    File file = new File( arg0.getId() );
    String content = "";
    try {

      InputStream in = new FileInputStream( file );

      StringWriter writer = new StringWriter();
      IOUtils.copy( in, writer );
      content = writer.toString();

      ByteArrayInputStream xmlStream = new ByteArrayInputStream( content.getBytes() );
      return new JobMeta( xmlStream, null, null );

    } catch ( Throwable e ) {
      e.printStackTrace();
    }

    return null;
  }

  private static TransMeta loadTransformation( ObjectId arg0, String arg1 ) throws KettleException {
    if ( delay != 0 ) {
      try {
        Thread.sleep( delay );
      } catch ( InterruptedException e ) {
      }
    }
    System.out.println( "loadJob " + arg0 );
    File file = new File( arg0.getId() );
    String content = "";
    try {

      InputStream in = new FileInputStream( file );

      StringWriter writer = new StringWriter();
      IOUtils.copy( in, writer );
      content = writer.toString();

      ByteArrayInputStream xmlStream = new ByteArrayInputStream( content.getBytes() );
      return new TransMeta( xmlStream, null, false, null, null );

    } catch ( Throwable e ) {
      // intentional
      return null;
    }
  }

  /**
   * ************ end -- load job and trans methods for the mock diRepo ****************
   */

  public static RepositoryFileTree getTree( RepositoryRequest req ) {

    File root = new File( SOLUTION_PATH );
    RepositoryFileTree rft = createFileTree( root );

    return rft;
  }

  private static RepositoryFileTree createFileTree( File root ) {

    RepositoryFile repFile = new RepositoryFile( root.getPath(), root.getName(),
        root.isDirectory(), false,
        false, null, root.getAbsolutePath(), new Date( root.lastModified() ),
        new Date( root.lastModified() ), false, null, null, null, null, root.getName(),
        null, null, null, root.length(), "Admin", null );
    List<RepositoryFileTree> children = new ArrayList<RepositoryFileTree>();

    File[] files = root.listFiles();
    for ( File file : files ) {
      if ( file.isHidden() ) {
        continue;
      }

      if ( file.isDirectory() ) {
        RepositoryFileTree kid = createFileTree( file );
        children.add( kid );
      } else if ( file.isFile() ) {
        RepositoryFile kid = new RepositoryFile( file.getPath(), file.getName(),
            file.isDirectory(), false,
            false, null, file.getPath(), new Date( file.lastModified() ),
            new Date( file.lastModified() ), false, null, null, null, null, file.getName(),
            null, null, null, root.length(), "Admin", null );
        RepositoryFileTree kidTree = new RepositoryFileTree( kid, null );
        children.add( kidTree );
      }
    }
    RepositoryFileTree fileTree = new RepositoryFileTree( repFile, children );
    return fileTree;
  }

  private static class FakePurRepository extends KettleFileRepository {

    private IUnifiedRepository backingRepo;

    public FakePurRepository( IUnifiedRepository backingRepo ) {
      this.backingRepo = backingRepo;
    }

    public IUnifiedRepository getPur() {
      return backingRepo;
    }

    @Override
    public JobMeta loadJob( ObjectId idJob, String versionLabel ) throws KettleException {
      return LocatorTestUtils.loadJob( idJob, versionLabel );
    }

    @Override
    public TransMeta loadTransformation( ObjectId idTransformation, String versionLabel )
        throws KettleException {
      return LocatorTestUtils.loadTransformation( idTransformation, versionLabel );
    }
  }

}

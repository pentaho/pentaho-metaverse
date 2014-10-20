/*!
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
package com.pentaho.metaverse.locator;

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

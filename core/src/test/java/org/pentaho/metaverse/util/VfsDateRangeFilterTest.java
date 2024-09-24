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

package org.pentaho.metaverse.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileType;
import org.junit.Before;
import org.junit.Test;

public class VfsDateRangeFilterTest {

  VfsDateRangeFilter filter;
  SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd" );
  Date now = new Date();
  Date then = new Date();
  String start = "20150708";
  String between = "20150709";
  String end = "20150710";

  @Before
  public void setUp() throws Exception {
    format.setLenient( false );
  }

  @Test
  public void testConstructor() throws Exception {
    filter = new VfsDateRangeFilter( format );
    assertEquals( format, filter.format );
    assertNull( filter.getEndingDate() );
    assertNull( filter.getStartingDate() );
  }

  @Test
  public void testContructor_startDate() throws Exception {
    filter = new VfsDateRangeFilter( format, now );
    assertEquals( format, filter.format );
    assertNull( filter.getEndingDate() );
    assertEquals( now, filter.getStartingDate() );
  }

  @Test
  public void testContructor_startDate_endDate() throws Exception {
    filter = new VfsDateRangeFilter( format, now, then );
    assertEquals( format, filter.format );
    assertEquals( now, filter.getStartingDate() );
    assertEquals( then, filter.getEndingDate() );
  }

  @Test
  public void testContructor_startDateString() throws Exception {
    filter = new VfsDateRangeFilter( format, start );
    assertEquals( format, filter.format );
    assertEquals( start, format.format( filter.getStartingDate() ) );
    assertNull( filter.getEndingDate() );
  }

  @Test
  public void testContructor_startDateString_endDateString() throws Exception {
    filter = new VfsDateRangeFilter( format, start, end );
    assertEquals( format, filter.format );
    assertEquals( start, format.format( filter.getStartingDate() ) );
    assertEquals( end, format.format( filter.getEndingDate() ) );
  }

  @Test
  public void testGettersSetters() throws Exception {
    filter = new VfsDateRangeFilter( format );
    filter.setStartingDate( now );
    assertEquals( now, filter.getStartingDate() );
    filter.setEndingDate( then );
    assertEquals( then, filter.getEndingDate() );
  }

  @Test
  public void testParseDateString() throws Exception {
    filter = new VfsDateRangeFilter( format );
    Date date = filter.parseDateString( start );
    assertNotNull( date );

    Calendar cal = Calendar.getInstance();
    cal.setTime( date );
    assertEquals( 2015, cal.get( Calendar.YEAR ) );
    assertEquals( Calendar.JULY, cal.get( Calendar.MONTH ) );
    assertEquals( 8, cal.get( Calendar.DAY_OF_MONTH ) );
  }

  @Test
  public void testParseNullDateString() throws Exception {
    filter = new VfsDateRangeFilter( format );
    Date date = filter.parseDateString( null );
    assertNull( date );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testParseInvalidDateString() throws Exception {
    filter = new VfsDateRangeFilter( format );
    Date date = filter.parseDateString( "20159999" );
  }

  @Test
  public void testAccept_notFolder() throws Exception {
    filter = new VfsDateRangeFilter( format, start );
    FileSelectInfo fsi = mock( FileSelectInfo.class );
    FileObject fo = mock( FileObject.class );
    when( fo.getType() ).thenReturn( FileType.FILE );
    when( fsi.getFile() ).thenReturn( fo );
    when( fsi.getDepth() ).thenReturn( 1 );
    assertFalse( filter.includeFile( fsi ) );
  }

  @Test
  public void testAccept_startDateSet() throws Exception {
    filter = new VfsDateRangeFilter( format, start );
    FileSelectInfo fsi = mock( FileSelectInfo.class );
    FileObject fo = mock( FileObject.class );
    FileName fn = mock( FileName.class );
    when( fn.getBaseName() ).thenReturn( end );
    when( fo.getType() ).thenReturn( FileType.FOLDER );
    when( fo.getName() ).thenReturn( fn );
    when( fsi.getFile() ).thenReturn( fo );
    when( fsi.getDepth() ).thenReturn( 1 );
    assertTrue( filter.includeFile( fsi ) );
    when( fn.getBaseName() ).thenReturn( start );
    assertTrue( "Start date is not inclusive", filter.includeFile( fsi ) );
    when( fn.getBaseName() ).thenReturn( "20000101" );
    assertFalse( "Before start date was accepted", filter.includeFile( fsi ) );
  }

  @Test
  public void testAccept_startDateSet_endDateSet() throws Exception {
    filter = new VfsDateRangeFilter( format, start, end );
    FileSelectInfo fsi = mock( FileSelectInfo.class );
    FileObject fo = mock( FileObject.class );
    FileName fn = mock( FileName.class );
    when( fn.getBaseName() ).thenReturn( end );
    when( fo.getType() ).thenReturn( FileType.FOLDER );
    when( fo.getName() ).thenReturn( fn );
    when( fsi.getFile() ).thenReturn( fo );
    when( fsi.getDepth() ).thenReturn( 1 );
    assertTrue( "End date is not inclusive", filter.includeFile( fsi ) );
    when( fn.getBaseName() ).thenReturn( start );
    assertTrue( "Start date is not inclusive", filter.includeFile( fsi ) );
    when( fn.getBaseName() ).thenReturn( between );
    assertTrue( "Between start and end date is not accepted", filter.includeFile( fsi ) );
    when( fn.getBaseName() ).thenReturn( "20000101" );
    assertFalse( "Before start date was accepted", filter.includeFile( fsi ) );
    when( fn.getBaseName() ).thenReturn( "21000101" );
    assertFalse( "After end date was accepted", filter.includeFile( fsi ) );
  }

  @Test
  public void testAccept_startDateNull_endDateSet() throws Exception {
    filter = new VfsDateRangeFilter( format, null, end );
    FileSelectInfo fsi = mock( FileSelectInfo.class );
    FileObject fo = mock( FileObject.class );
    FileName fn = mock( FileName.class );
    when( fn.getBaseName() ).thenReturn( end );
    when( fo.getType() ).thenReturn( FileType.FOLDER );
    when( fo.getName() ).thenReturn( fn );
    when( fsi.getFile() ).thenReturn( fo );
    when( fsi.getDepth() ).thenReturn( 1 );
    assertTrue( "End date is not inclusive", filter.includeFile( fsi ) );
    when( fn.getBaseName() ).thenReturn( "20000101" );
    assertTrue( "Before end date was not accepted", filter.includeFile( fsi ) );
    when( fn.getBaseName() ).thenReturn( "21000101" );
    assertFalse( "After end date was accepted", filter.includeFile( fsi ) );
  }

  @Test
  public void testAccept_invalidDateStringFolder() throws Exception {
    filter = new VfsDateRangeFilter( format, start, end );
    filter = new VfsDateRangeFilter( format, null, end );
    FileSelectInfo fsi = mock( FileSelectInfo.class );
    FileObject fo = mock( FileObject.class );
    FileName fn = mock( FileName.class );
    when( fn.getBaseName() ).thenReturn( "not a date" );
    when( fo.getType() ).thenReturn( FileType.FOLDER );
    when( fo.getName() ).thenReturn( fn );
    when( fsi.getFile() ).thenReturn( fo );
    when( fsi.getDepth() ).thenReturn( 1 );
    assertFalse( "Invalid date was accepted", filter.includeFile( fsi ) );
  }
}

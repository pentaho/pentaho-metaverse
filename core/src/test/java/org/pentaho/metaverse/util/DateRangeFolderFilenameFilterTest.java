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

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DateRangeFolderFilenameFilterTest {

  DateRangeFolderFilenameFilter filter;
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
    filter = new DateRangeFolderFilenameFilter( format );
    assertEquals( format, filter.format );
    assertNull( filter.getEndingDate() );
    assertNull( filter.getStartingDate() );
  }

  @Test
  public void testContructor_startDate() throws Exception {
    filter = new DateRangeFolderFilenameFilter( format, now );
    assertEquals( format, filter.format );
    assertNull( filter.getEndingDate() );
    assertEquals( now, filter.getStartingDate() );
  }

  @Test
  public void testContructor_startDate_endDate() throws Exception {
    filter = new DateRangeFolderFilenameFilter( format, now, then );
    assertEquals( format, filter.format );
    assertEquals( now, filter.getStartingDate() );
    assertEquals( then, filter.getEndingDate() );
  }

  @Test
  public void testContructor_startDateString() throws Exception {
    filter = new DateRangeFolderFilenameFilter( format, start );
    assertEquals( format, filter.format );
    assertEquals( start, format.format( filter.getStartingDate() ) );
    assertNull( filter.getEndingDate() );
  }

  @Test
  public void testContructor_startDateString_endDateString() throws Exception {
    filter = new DateRangeFolderFilenameFilter( format, start, end );
    assertEquals( format, filter.format );
    assertEquals( start, format.format( filter.getStartingDate() ) );
    assertEquals( end, format.format( filter.getEndingDate() ) );
  }

  @Test
  public void testGettersSetters() throws Exception {
    filter = new DateRangeFolderFilenameFilter( format );
    filter.setStartingDate( now );
    assertEquals( now, filter.getStartingDate() );
    filter.setEndingDate( then );
    assertEquals( then, filter.getEndingDate() );
  }

  @Test
  public void testParseDateString() throws Exception {
    filter = new DateRangeFolderFilenameFilter( format );
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
    filter = new DateRangeFolderFilenameFilter( format );
    Date date = filter.parseDateString( null );
    assertNull( date );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testParseInvalidDateString() throws Exception {
    filter = new DateRangeFolderFilenameFilter( format );
    Date date = filter.parseDateString( "20159999" );
  }

  @Test
  public void testAccept_notFolder() throws Exception {
    filter = new DateRangeFolderFilenameFilter( format, start );
    File f = spy( new File( "" ) );
    when( f.isDirectory() ).thenReturn( false );
    assertFalse( filter.accept( f, end ) );
  }

  @Test
  public void testAccept_startDateSet() throws Exception {
    filter = new DateRangeFolderFilenameFilter( format, start );
    File f = spy( new File( "" ) );
    when( f.isDirectory() ).thenReturn( true );
    assertTrue( filter.accept( f, end ) );
    assertTrue( "Start date is not inclusive", filter.accept( f, start ) );
    assertFalse( "Before start date was accepted", filter.accept( f, "20000101" ) );
  }

  @Test
  public void testAccept_startDateSet_endDateSet() throws Exception {
    filter = new DateRangeFolderFilenameFilter( format, start, end );
    File f = spy( new File( "" ) );
    when( f.isDirectory() ).thenReturn( true );
    assertTrue( "End date is not inclusive", filter.accept( f, end ) );
    assertTrue( "Start date is not inclusive",  filter.accept( f, start ) );
    assertTrue( "Between start and end date is not accepted",  filter.accept( f, between ) );
    assertFalse( "Before start date was accepted", filter.accept( f, "20000101" ) );
    assertFalse( "After end date was accepted", filter.accept( f, "21000101" ) );
  }

  @Test
  public void testAccept_startDateNull_endDateSet() throws Exception {
    filter = new DateRangeFolderFilenameFilter( format, null, end );
    File f = spy( new File( "" ) );
    when( f.isDirectory() ).thenReturn( true );
    assertTrue( "End date is not inclusive", filter.accept( f, end ) );
    assertTrue( "Before end date was not accepted", filter.accept( f, "20000101" ) );
    assertFalse( "After end date was accepted", filter.accept( f, "21000101" ) );
  }

  @Test
  public void testAccept_invalidDateStringFolder() throws Exception {
    filter = new DateRangeFolderFilenameFilter( format, start, end );
    File f = spy( new File( "" ) );
    when( f.isDirectory() ).thenReturn( true );
    assertFalse( "Invalid date was accepted", filter.accept( f, "not a date" ) );
  }
}
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

import org.apache.commons.vfs2.FileDepthSelector;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileType;
import org.pentaho.metaverse.messages.Messages;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VfsDateRangeFilter extends FileDepthSelector {

  protected SimpleDateFormat format;
  private Date startingDate;
  private Date endingDate;

  public VfsDateRangeFilter( SimpleDateFormat format ) {
    super( 1, 256 );
    this.format = format;
  }

  public VfsDateRangeFilter( SimpleDateFormat format, Date startingDate ) {
    this( format );
    this.startingDate = startingDate;
  }

  public VfsDateRangeFilter( SimpleDateFormat format, String startingDate ) {
    this( format );
    setStartingDate( startingDate );
  }

  public VfsDateRangeFilter( SimpleDateFormat format, Date startingDate, Date endingDate ) {
    this( format, startingDate );
    this.endingDate = endingDate;
  }

  public VfsDateRangeFilter( SimpleDateFormat format, String startingDate, String endingDate ) {
    this( format, startingDate );
    setEndingDate( endingDate );
  }

  public Date getEndingDate() {
    return endingDate;
  }

  public void setEndingDate( Date endingDate ) {
    this.endingDate = endingDate;
  }

  public void setEndingDate( String endingDate ) {
    this.endingDate = parseDateString( endingDate );
  }

  public Date getStartingDate() {
    return startingDate;
  }

  public void setStartingDate( Date startingDate ) {
    this.startingDate = startingDate;
  }

  public void setStartingDate( String startingDate ) {
    this.startingDate = parseDateString( startingDate );
  }

  protected Date parseDateString( String dateString ) throws IllegalArgumentException {
    if ( dateString == null ) {
      return null;
    }
    try {
      return format.parse( dateString );
    } catch ( ParseException e ) {
      throw new IllegalArgumentException( Messages.getString( "ERROR.CouldNotParseDateFromString", dateString ), e );
    }
  }

  @Override
  public boolean includeFile( FileSelectInfo fileInfo ) {
    try {
      boolean result = super.includeFile( fileInfo );
      if ( fileInfo.getFile().getType() == FileType.FOLDER ) {

        Date folderDate = format.parse( fileInfo.getFile().getName().getBaseName() );

        // assume a match on start & end dates
        int startCompare = 0;
        int endCompare = 0;

        // it is a valid date, now, is it greater than or equal to the requested date?
        if ( startingDate != null ) {
          startCompare = folderDate.compareTo( startingDate );
        }
        if ( endingDate != null ) {
          endCompare = folderDate.compareTo( endingDate );
        }

        return startCompare >= 0 && endCompare <= 0 && result;
      } else {
        return false;
      }
    } catch ( Exception e ) {
      // folder name is not a valid date string, reject it
      // [PDI-17775] Doing a full exception catch due to the extended class throwing a general exception
      return false;
    }
  }
}

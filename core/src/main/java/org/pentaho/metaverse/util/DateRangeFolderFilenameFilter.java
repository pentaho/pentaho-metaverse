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

import org.pentaho.metaverse.messages.Messages;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @see org.pentaho.metaverse.impl.util.VfsDateRangeFilter
 * 
 * @deprecated Use above class as a direct replacement.  It provides backward compatibility
 * for the local file system, but allows use of VFS based file systems simply by changing
 * the lineage.execution.output.folder to a VFS supported specification path.
 */
@Deprecated
public class DateRangeFolderFilenameFilter implements FilenameFilter {

  protected SimpleDateFormat format;
  private Date startingDate;
  private Date endingDate;

  public DateRangeFolderFilenameFilter( SimpleDateFormat format ) {
    this.format = format;
  }

  public DateRangeFolderFilenameFilter( SimpleDateFormat format, Date startingDate ) {
    this.format = format;
    this.startingDate = startingDate;
  }

  public DateRangeFolderFilenameFilter( SimpleDateFormat format, String startingDate ) {
    this.format = format;
    setStartingDate( startingDate );
  }

  public DateRangeFolderFilenameFilter( SimpleDateFormat format, Date startingDate, Date endingDate ) {
    this.format = format;
    this.startingDate = startingDate;
    this.endingDate = endingDate;
  }

  public DateRangeFolderFilenameFilter( SimpleDateFormat format, String startingDate, String endingDate ) {
    this.format = format;
    setStartingDate( startingDate );
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
  public boolean accept( File dir, String name ) {
    if ( dir.isDirectory() ) {
      try {
        Date folderDate = format.parse( name );

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

        return startCompare >= 0 && endCompare <= 0;

      } catch ( ParseException e ) {
        // folder name is not a valid date string, reject it
        return false;
      }
    } else {
      return false;
    }
  }
}

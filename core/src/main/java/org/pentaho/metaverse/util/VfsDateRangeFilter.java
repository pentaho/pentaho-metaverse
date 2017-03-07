/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.vfs2.FileDepthSelector;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.pentaho.metaverse.messages.Messages;

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
    boolean result = super.includeFile( fileInfo );
    try {
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
    } catch ( ParseException | FileSystemException e ) {
      // folder name is not a valid date string, reject it
      return false;
    }
  }
}

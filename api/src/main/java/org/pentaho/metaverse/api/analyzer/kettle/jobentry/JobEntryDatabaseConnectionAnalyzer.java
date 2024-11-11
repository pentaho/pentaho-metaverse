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


package org.pentaho.metaverse.api.analyzer.kettle.jobentry;

import org.apache.commons.lang.ArrayUtils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.metaverse.api.analyzer.kettle.DatabaseConnectionAnalyzer;

import java.util.Arrays;
import java.util.List;

/**
 * User: RFellows Date: 3/6/15
 */
public class JobEntryDatabaseConnectionAnalyzer extends DatabaseConnectionAnalyzer<JobEntryInterface> {

  @Override
  public List<DatabaseMeta> getUsedConnections( JobEntryInterface meta ) {
    if ( meta != null ) {
      DatabaseMeta[] dbMetas = meta.getUsedDatabaseConnections();
      List<DatabaseMeta> databaseMetas = null;
      if ( !ArrayUtils.isEmpty( dbMetas ) ) {
        databaseMetas = Arrays.asList( dbMetas );
      }
      return databaseMetas;
    } else {
      return null;
    }
  }

}

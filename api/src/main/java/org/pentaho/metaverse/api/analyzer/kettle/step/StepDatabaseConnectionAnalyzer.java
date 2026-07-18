/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.apache.commons.lang3.ArrayUtils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.api.analyzer.kettle.DatabaseConnectionAnalyzer;

import java.util.Arrays;
import java.util.List;

/**
 * User: RFellows Date: 3/6/15
 */
public class StepDatabaseConnectionAnalyzer extends DatabaseConnectionAnalyzer<BaseStepMeta> {

  @Override
  public List<DatabaseMeta> getUsedConnections( BaseStepMeta meta ) {
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

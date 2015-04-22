package com.pentaho.metaverse.api.analyzer.kettle.step;

import com.pentaho.metaverse.api.analyzer.kettle.DatabaseConnectionAnalyzer;
import org.apache.commons.lang.ArrayUtils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.BaseStepMeta;

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

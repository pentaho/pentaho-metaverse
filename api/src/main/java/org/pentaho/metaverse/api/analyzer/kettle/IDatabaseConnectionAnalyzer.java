package org.pentaho.metaverse.api.analyzer.kettle;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metaverse.api.IConnectionAnalyzer;

/**
 * Created by mburgess on 8/6/14.
 */
public interface IDatabaseConnectionAnalyzer<T> extends IConnectionAnalyzer<DatabaseMeta, T> {
}

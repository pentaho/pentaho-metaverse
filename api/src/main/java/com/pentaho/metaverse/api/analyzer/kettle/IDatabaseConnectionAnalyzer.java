package com.pentaho.metaverse.api.analyzer.kettle;

import com.pentaho.metaverse.api.IConnectionAnalyzer;
import org.pentaho.di.core.database.DatabaseMeta;

/**
 * Created by mburgess on 8/6/14.
 */
public interface IDatabaseConnectionAnalyzer<T> extends IConnectionAnalyzer<DatabaseMeta, T> {
}

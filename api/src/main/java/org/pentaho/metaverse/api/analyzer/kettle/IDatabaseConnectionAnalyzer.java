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



package org.pentaho.metaverse.api.analyzer.kettle;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metaverse.api.IConnectionAnalyzer;

/**
 * Created by mburgess on 8/6/14.
 */
public interface IDatabaseConnectionAnalyzer<T> extends IConnectionAnalyzer<DatabaseMeta, T> {
}

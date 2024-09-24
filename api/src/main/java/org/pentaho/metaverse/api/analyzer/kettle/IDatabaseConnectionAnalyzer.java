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

package org.pentaho.metaverse.api.analyzer.kettle;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metaverse.api.IConnectionAnalyzer;

/**
 * Created by mburgess on 8/6/14.
 */
public interface IDatabaseConnectionAnalyzer<T> extends IConnectionAnalyzer<DatabaseMeta, T> {
}

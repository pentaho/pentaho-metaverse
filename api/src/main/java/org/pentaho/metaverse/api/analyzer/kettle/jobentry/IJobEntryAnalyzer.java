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

import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.metaverse.api.IAnalyzer;

import java.util.Set;

/**
 * Created by mburgess on 7/29/14.
 */
public interface IJobEntryAnalyzer<S, T extends JobEntryInterface> extends IAnalyzer<S, T> {

  /**
   * Gets the set of job entry classes that can be analyzed by this analyzer
   *
   * @return a Set of JobEntryCopy classes that this analyzer supports
   */
  Set<Class<? extends JobEntryInterface>> getSupportedEntries();
}

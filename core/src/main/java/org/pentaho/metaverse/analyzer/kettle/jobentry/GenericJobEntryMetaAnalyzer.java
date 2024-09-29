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


package org.pentaho.metaverse.analyzer.kettle.jobentry;

import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.JobEntryAnalyzer;

import java.util.Set;

/**
 * KettleGenericStepMetaAnalyzer provides a default implementation for analyzing PDI step to gather metadata for the
 * metaverse.
 */
public class GenericJobEntryMetaAnalyzer extends JobEntryAnalyzer<JobEntryInterface> {

  /**
   * Analyzes a step to gather metadata (such as input/output fields, used database connections, etc.)
   *
   * @see IAnalyzer#analyze(IComponentDescriptor, Object)
   */
  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, JobEntryInterface jobEntryInterface )
    throws MetaverseAnalyzerException {

    return super.analyze( descriptor, jobEntryInterface );
  }

  @Override
  public Set<Class<? extends JobEntryInterface>> getSupportedEntries() {
    return null;
  }

  @Override
  protected void customAnalyze( JobEntryInterface entry, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    // TODO Auto-generated method stub

  }

}

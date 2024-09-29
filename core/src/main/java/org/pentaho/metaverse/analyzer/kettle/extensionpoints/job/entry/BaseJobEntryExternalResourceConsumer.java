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


package org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.entry;

import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.AnalysisContext;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;
import java.util.Collections;

/**
 * This class is a do-nothing reference implementation for StepExternalConsumer plugins. Subclasses should override
 * the various methods with business logic that can handle the external resources used by the given step.
 */
public abstract class BaseJobEntryExternalResourceConsumer<T extends JobEntryBase>
  implements IJobEntryExternalResourceConsumer<T> {

  @Override
  public boolean isDataDriven( T meta ) {
    return false;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( T meta ) {
    return getResourcesFromMeta( meta, new AnalysisContext( DictionaryConst.CONTEXT_RUNTIME ) );
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( T consumer, IAnalysisContext context ) {
    return Collections.emptyList();
  }
}

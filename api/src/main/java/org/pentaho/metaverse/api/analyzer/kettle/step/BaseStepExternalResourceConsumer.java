/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputStep;
import org.pentaho.di.trans.steps.file.BaseFileMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.AnalysisContext;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;
import java.util.HashSet;

/**
 * This class is a base implementation for StepExternalConsumer plugins. Subclasses should override the various methods
 * with business logic that can handle the external resources used by the given step.
 */
public abstract class BaseStepExternalResourceConsumer<S extends BaseStep, M extends BaseStepMeta>
  implements IStepExternalResourceConsumer<S, M> {

  /**
   * Returns true when resources should be fetched. Resources are fetched when they are expected to be resolved (true by
   * default), and in the case of {@link BaseFileMeta}, when the step writes to a file, otherwise false is returned.
   */
  private boolean fetchResources( final M meta ) {
    return !( meta instanceof BaseFileMeta ) || ( ( (BaseFileMeta) meta ).writesToFile() );
  }

  @Override
  public boolean isDataDriven( M meta ) {
    return false;
  }

  // it's important that this method exists because there are subclasses that only implement
  // getResourcesFromMeta( meta, IAnalysisContext), but call this method.
  @Deprecated
  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( final M meta ) {
    return getResourcesFromMeta( meta, new AnalysisContext( DictionaryConst.CONTEXT_RUNTIME ) );
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( Bowl bowl, final M meta ) {
    return getResourcesFromMeta( bowl, meta, new AnalysisContext( DictionaryConst.CONTEXT_RUNTIME ) );
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta(
    Bowl bowl, final M meta, final IAnalysisContext context ) {

    if ( !( meta instanceof BaseFileMeta ) || !fetchResources( meta ) ) {
      return new HashSet();
    }

    return KettleAnalyzerUtil.getResourcesFromMeta( bowl, meta,
      isDataDriven( meta ) ? new String[]{} : ( (BaseFileMeta) meta ).getFilePaths( false ) );
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromRow(
    final S step, final RowMetaInterface rowMeta, final Object[] row ) {

    if ( !fetchResources( null ) || !( step instanceof BaseFileInputStep ) ) {
      return new HashSet();
    }
    return KettleAnalyzerUtil.getResourcesFromRow( step.getTransMeta().getBowl(),
      (BaseFileInputStep) step, rowMeta, row );
  }
}

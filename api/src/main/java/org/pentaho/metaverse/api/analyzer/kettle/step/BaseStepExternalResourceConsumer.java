/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputStep;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.AnalysisContext;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.IMetaverseConfig;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is a base implementation for StepExternalConsumer plugins. Subclasses should override the various methods
 * with business logic that can handle the external resources used by the given step.
 */
public abstract class BaseStepExternalResourceConsumer<S extends BaseStep, M extends BaseStepMeta>
  implements IStepExternalResourceConsumer<S, M> {

  private Set<IExternalResourceInfo> resourcesFromRow = new HashSet<>();

  protected boolean resolveExternalResources() {
    return true;
  }

  @Override
  public boolean isDataDriven( M meta ) {
    return false;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( M meta ) {
    return getResourcesFromMeta( meta, new AnalysisContext( DictionaryConst.CONTEXT_RUNTIME ) );
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( final M meta, final IAnalysisContext context ) {
    if ( !resolveExternalResources() ) {
      return Collections.emptyList();
    }

    if ( meta instanceof BaseFileInputMeta && !isDataDriven( meta ) ) {
      return KettleAnalyzerUtil.getResourcesFromMeta( (BaseFileInputMeta) meta, context );
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public Collection<IExternalResourceInfo> getResources( final M meta, final IAnalysisContext context ) {
    if ( !resolveExternalResources() ) {
      return Collections.emptyList();
    }

    Collection<IExternalResourceInfo> allResources = getResourcesFromMeta( meta, context );
    if ( allResources.isEmpty() ) {
      allResources = new ArrayList<>( this.resourcesFromRow.size() );
    }
    for ( final IExternalResourceInfo resource : this.resourcesFromRow ) {
      allResources.add( resource );
    }
    return allResources;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromRow(
    final S step, final RowMetaInterface rowMeta, final Object[] row ) {
    if ( !resolveExternalResources() ) {
      return Collections.emptyList();
    }

    if ( step instanceof BaseFileInputStep ) {
      Collection<IExternalResourceInfo> resourcesFromRow = KettleAnalyzerUtil.getResourcesFromRow(
        (BaseFileInputStep) step, rowMeta, row );
      // keep track of resources from row, as they are encountered - we do this, because this method is called for
      // each row, and we need to keep track of all of them
      for ( final IExternalResourceInfo resource : resourcesFromRow ) {
        this.resourcesFromRow.add( resource );
      }
      return resourcesFromRow;
    } else {
      return Collections.emptyList();
    }
  }
}

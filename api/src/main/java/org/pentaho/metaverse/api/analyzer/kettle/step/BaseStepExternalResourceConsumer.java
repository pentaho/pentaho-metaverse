/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.AnalysisContext;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;
import java.util.Collections;

/**
 * This class is a do-nothing reference implementation for StepExternalConsumer plugins. Subclasses should override
 * the various methods with business logic that can handle the external resources used by the given step.
 */
public abstract class BaseStepExternalResourceConsumer<S extends BaseStep, M extends BaseStepMeta>
  implements IStepExternalResourceConsumer<S, M> {

  @Override
  public boolean isDataDriven( M meta ) {
    return false;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( M meta ) {
    return getResourcesFromMeta( meta, new AnalysisContext( DictionaryConst.CONTEXT_RUNTIME ) );
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( M consumer, IAnalysisContext context ) {
    return Collections.emptyList();
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromRow( S step, RowMetaInterface rowMeta, Object[] row ) {
    return Collections.emptyList();
  }
}

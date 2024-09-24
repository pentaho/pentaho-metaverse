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
import org.pentaho.metaverse.api.analyzer.kettle.IExternalResourceConsumer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;

/**
 * IStepExternalResourceConsumer is a helper interface used by ExternalResourceConsumer plugins that handle a single
 * PDI step type (see the parameterized type in declaration)
 *
 * @param <M> The type of step that will consume external resources
 */
public interface IStepExternalResourceConsumer<S extends BaseStep, M extends BaseStepMeta>
  extends IExternalResourceConsumer<M> {

  Collection<IExternalResourceInfo> getResourcesFromRow( S consumer, RowMetaInterface rowMeta, Object[] row );
}

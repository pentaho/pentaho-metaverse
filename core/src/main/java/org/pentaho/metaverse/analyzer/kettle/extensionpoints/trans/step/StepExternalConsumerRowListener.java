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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.step;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransLineageHolderMap;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StepExternalConsumerRowListener extends RowAdapter {

  private final IStepExternalResourceConsumer stepExternalResourceConsumer;
  private final StepInterface step;

  public StepExternalConsumerRowListener(
    IStepExternalResourceConsumer stepExternalResourceConsumer, StepInterface step ) {
    this.stepExternalResourceConsumer = stepExternalResourceConsumer;
    this.step = step;
  }

  /**
   * Called when rows are read by the step to which this listener is attached
   *
   * @param rowMeta The metadata (value types, e.g.) of the associated row data
   * @param row     An array of Objects corresponding to the row data
   * @see org.pentaho.di.trans.step.RowListener#rowReadEvent(org.pentaho.di.core.row.RowMetaInterface,
   * Object[])
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public void rowReadEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {

    Collection<IExternalResourceInfo> resources =
      stepExternalResourceConsumer.getResourcesFromRow( (BaseStep) step, rowMeta, row );
    if ( resources != null ) {
      // Add the resources to the execution profile
      IExecutionProfile executionProfile =
        TransLineageHolderMap.getInstance().getLineageHolder( step.getTrans() ).getExecutionProfile();
      if ( executionProfile != null ) {
        String stepName = step.getStepname();
        Map<String, List<IExternalResourceInfo>> resourceMap =
          executionProfile.getExecutionData().getExternalResources();
        List<IExternalResourceInfo> externalResources = resourceMap.get( stepName );
        if ( externalResources == null ) {
          externalResources = new LinkedList<IExternalResourceInfo>();
        }
        externalResources.addAll( resources );
        resourceMap.put( stepName, externalResources );
      }
    }
  }
}

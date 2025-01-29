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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        final List<IExternalResourceInfo> existingResources = resourceMap.get( stepName );
        Set<IExternalResourceInfo> externalResources = existingResources == null ? new HashSet()
          : new HashSet( resourceMap.get( stepName ) );
        if ( externalResources == null ) {
          externalResources = new HashSet();
        }
        // avoid adding duplicates
        externalResources.addAll( new HashSet<>( resources ) );
        resourceMap.put( stepName, new ArrayList( externalResources ) );
      }
    }
  }
}

/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.step;

import com.pentaho.metaverse.analyzer.kettle.extensionpoints.IStepExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransformationRuntimeExtensionPoint;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;

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
      IExecutionProfile executionProfile = TransformationRuntimeExtensionPoint.getProfileMap().get( step.getTrans() );
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

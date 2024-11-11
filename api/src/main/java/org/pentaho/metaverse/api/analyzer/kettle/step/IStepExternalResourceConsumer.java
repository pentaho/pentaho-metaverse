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

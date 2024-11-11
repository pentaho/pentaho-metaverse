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


package org.pentaho.metaverse.api.analyzer.kettle.jobentry;


import org.pentaho.metaverse.api.IExternalResourceConsumerProvider;

/**
 * This is a marker interface for external resource consumer providers that provide JobEntryExternalResourceConsumers.
 */
public interface IJobEntryExternalResourceConsumerProvider extends
  IExternalResourceConsumerProvider<IJobEntryExternalResourceConsumer> {
}

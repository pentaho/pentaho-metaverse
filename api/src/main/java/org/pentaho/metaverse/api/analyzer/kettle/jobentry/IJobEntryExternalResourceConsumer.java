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


package org.pentaho.metaverse.api.analyzer.kettle.jobentry;

import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.metaverse.api.analyzer.kettle.IExternalResourceConsumer;

public interface IJobEntryExternalResourceConsumer<T extends JobEntryBase>
  extends IExternalResourceConsumer<T> {
}

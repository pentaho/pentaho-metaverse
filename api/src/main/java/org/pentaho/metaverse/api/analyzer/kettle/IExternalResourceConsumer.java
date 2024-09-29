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


package org.pentaho.metaverse.api.analyzer.kettle;

import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;

/**
 * The IExternalResourceConsumer interface allows consumers of external resources to report the usages to those that
 * are interested.
 */
public interface IExternalResourceConsumer<T> extends MetaClassProvider<T>, Cloneable {

  boolean isDataDriven( T consumer );

  Collection<IExternalResourceInfo> getResourcesFromMeta( T consumer );
  Collection<IExternalResourceInfo> getResourcesFromMeta( T consumer, IAnalysisContext context );
}

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


package org.pentaho.metaverse.api.analyzer.kettle;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;

/**
 * The IExternalResourceConsumer interface allows consumers of external resources to report the usages to those that
 * are interested.
 */
public interface IExternalResourceConsumer<T> extends MetaClassProvider<T>, Cloneable {

  boolean isDataDriven( T consumer );

  // Old default methods for old implementations to implement and and new callers to call through the new defaults.
  // New methods with Bowl for current implementations to implement and old callers to call through the old defaults.
  // Implementations must implement one set, but see also
  // {@link org.pentaho.metaverse.api.analyzer.kettle.steps.BaseStepExternalResourceConsumer
  // BaseStepExternalResourceConsumer}

  @Deprecated
  default Collection<IExternalResourceInfo> getResourcesFromMeta( T consumer ) {
    return getResourcesFromMeta( DefaultBowl.getInstance(), consumer );
  }

  @Deprecated
  default Collection<IExternalResourceInfo> getResourcesFromMeta( T consumer, IAnalysisContext context ) {
    return getResourcesFromMeta( DefaultBowl.getInstance(), consumer, context );
  }

  default Collection<IExternalResourceInfo> getResourcesFromMeta( Bowl bowl, T consumer ) {
    return getResourcesFromMeta( consumer );
  }

  default Collection<IExternalResourceInfo> getResourcesFromMeta( Bowl bowl, T consumer, IAnalysisContext context ) {
    return getResourcesFromMeta( consumer, context );
  }
}

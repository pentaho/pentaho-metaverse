/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.analyzer.kettle;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;

/**
 * The IExternalResourceConsumer interface allows consumers of external resources to report the usages to those that
 * are interested.
 */
public interface IExternalResourceConsumer<T> extends MetaClassProvider<T>, Cloneable {

  boolean isDataDriven( T consumer );

  Collection<IExternalResourceInfo> getResourcesFromMeta( Bowl bowl, T consumer );
  Collection<IExternalResourceInfo> getResourcesFromMeta( Bowl bowl, T consumer, IAnalysisContext context );
}

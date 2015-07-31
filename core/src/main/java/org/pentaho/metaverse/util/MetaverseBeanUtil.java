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

package org.pentaho.metaverse.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;

import java.util.Collection;

/**
 * MetaverseBeanUtil is a type of object factory that allows users to get blueprint-injected instances of objects
 */
public class MetaverseBeanUtil {

  private static MetaverseBeanUtil INSTANCE = new MetaverseBeanUtil();

  private BundleContext bundleContext;

  private MetaverseBeanUtil() {
    // private for singleton pattern
  }

  public static MetaverseBeanUtil getInstance() {
    return INSTANCE;
  }

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public Object get( String id ) {
    BlueprintContainer service = null;
    if ( bundleContext == null ) {
      return null;
    } else {
      try {
        Bundle bundle = bundleContext.getBundle();
        if ( bundle == null ) {
          return null;
        }
        Collection<ServiceReference<BlueprintContainer>> serviceReferences =
          bundleContext.getServiceReferences( BlueprintContainer.class,
            "(osgi.blueprint.container.symbolicname=" + bundle.getSymbolicName() + ")" );
        if ( serviceReferences.size() != 0 ) {
          ServiceReference<BlueprintContainer> reference = serviceReferences.iterator().next();
          service = bundleContext.getService( reference );
        }
      } catch ( InvalidSyntaxException e ) {
        // No-op, service will be null
      }
      if ( service == null ) {
        return null;
      }
      return service.getComponentInstance( id );
    }
  }

}

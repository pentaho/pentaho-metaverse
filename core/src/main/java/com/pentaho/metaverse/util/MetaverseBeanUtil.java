/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.util;

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
    try {
      Bundle bundle = this.bundleContext.getBundle();

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

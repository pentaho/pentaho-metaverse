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


package org.pentaho.metaverse.api;

import org.pentaho.metaverse.api.analyzer.kettle.IExternalResourceConsumer;

import java.util.Collection;
import java.util.List;

/**
 * 
 */
public interface IExternalResourceConsumerProvider<T extends IExternalResourceConsumer> {

  /**
   * Return the set of external resource consumers for this type
   * 
   * @return The analyzers
   */
  List<T> getExternalResourceConsumers();

  /**
   * Return the set of external resource consumers for this type for a given set of classes
   * 
   * @param types The set of classes to filter by
   * @return The external resource consumers
   */
  List<T> getExternalResourceConsumers( Collection<Class<?>> types );

  /**
   * Adds an external resource consumer to group of supported consumers
   * @param externalResourceConsumer
   */
  void addExternalResourceConsumer( T externalResourceConsumer );

  /**
   * Removes an externalResourceConsumer from the group of supported consumers
   * @param externalResourceConsumer
   */
  void removeExternalResourceConsumer( T externalResourceConsumer );

}

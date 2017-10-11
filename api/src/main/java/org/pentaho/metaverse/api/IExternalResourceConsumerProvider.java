/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

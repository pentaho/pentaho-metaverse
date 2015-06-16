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

package org.pentaho.metaverse.api;

import java.util.Collection;
import java.util.List;

/**
 * 
 * @author mburgess
 *
 * @param <T>
 */
public interface IAnalyzerProvider<T> {

  /**
   * Return the set of analyzers for this type
   * 
   * @return The analyzers
   */
  List<T> getAnalyzers();

  /**
   * Return the set of analyzers for this type for a given set of classes
   * 
   * @param types The set of classes to filter by
   * @return The analyzers
   */
  List<T> getAnalyzers( Collection<Class<?>> types );

  /**
   * Adds an analyzer to group of supported analyzers
   * @param analyzer
   */
  void addAnalyzer( T analyzer );

  /**
   * Removes an analyzer from the group supported analyzers
   * @param analyzer
   */
  void removeAnalyzer( T analyzer );

}

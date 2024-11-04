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

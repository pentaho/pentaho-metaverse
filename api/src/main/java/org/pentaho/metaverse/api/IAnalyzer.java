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

/**
 * The IAnalyzer interface provides methods for analyzing types of content (with a goal of integrating with the
 * metaverse)
 * 
 */
public interface IAnalyzer<S, T> extends IRequiresMetaverseBuilder {

  /**
   * Analyze the given object.
   * 
   * @param object
   *          the object
   * @return the root node resulting from the analysis of the specified object
   * @throws MetaverseAnalyzerException
   *           the metaverse analyzer exception
   */
  S analyze( IComponentDescriptor descriptor, T object ) throws MetaverseAnalyzerException;

}

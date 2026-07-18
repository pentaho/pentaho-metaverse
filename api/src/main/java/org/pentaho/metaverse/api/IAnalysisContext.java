/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.metaverse.api;

/**
 * The IAnalysisContext interface provides a way of associating a name and context object for
 * the purposes of analysis (static analysis, runtime analysis, e.g.)
 */
public interface IAnalysisContext {

  /**
   * Gets the name of this context
   *
   * @return the String name of the context
   */
  String getContextName();

  /**
   * Gets the object associated with this context
   *
   * @return the Object associated with the context (Trans for runtime analysis, e.g.)
   */
  Object getContextObject();
}

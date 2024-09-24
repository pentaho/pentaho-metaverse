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

import java.util.Set;

/**
 * The IDocumentAnalyzer interface represents an object capable of analyzing certain types of documents.
 * 
 */
public interface IDocumentAnalyzer<S> extends IAnalyzer<S, IDocument> {

  /**
   * Gets the types of documents supported by this analyzer
   * 
   * @return the supported types
   */
  Set<String> getSupportedTypes();

}

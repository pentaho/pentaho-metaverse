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

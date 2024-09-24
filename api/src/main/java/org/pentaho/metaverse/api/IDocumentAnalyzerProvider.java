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

import java.util.List;

/**
 * The IDocumentAnalyzerProvider provides known IDocumentAnalyzers of the system
 */
public interface IDocumentAnalyzerProvider extends IAnalyzerProvider<IDocumentAnalyzer> {

  /**
   * Gets the IDocumentAnalyzer(s) that support a specific type
   * @param type Specific type of analyzers interested in
   * @return The document analyzers
   */
  List<IDocumentAnalyzer> getDocumentAnalyzers( String type );

}

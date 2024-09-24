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
 * IDocumentController provides an interface for interacting with document controller instances. These instances are
 * responsible for (among other things) maintaining collection(s) of document analyzers.
 */
public interface IDocumentController extends IDocumentAnalyzerProvider, IRequiresMetaverseBuilder, IMetaverseBuilder {

  /**
   * Set the analyzers that are available in the system
   *
   * @param documentAnalyzers the complete Set of IDocumentAnalyzers
   */
  public void setDocumentAnalyzers( List<IDocumentAnalyzer> documentAnalyzers );
}

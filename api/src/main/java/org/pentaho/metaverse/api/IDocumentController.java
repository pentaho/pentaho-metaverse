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

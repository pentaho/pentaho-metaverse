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

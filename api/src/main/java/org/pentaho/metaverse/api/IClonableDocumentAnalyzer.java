/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.base.AbstractMeta;

public interface IClonableDocumentAnalyzer<S> extends IDocumentAnalyzer<S> {

  /**
   * Clones this {@link IClonableDocumentAnalyzer}.
   *
   * @return a clone of this {@link IClonableDocumentAnalyzer}
   */
  IClonableDocumentAnalyzer cloneAnalyzer();

  /**
   * Analyzes the document and all its steps.
   *
   * @param documentDescriptor the {@link IComponentDescriptor} for the document being analyzed
   * @param meta               the {@link AbstractMeta} representing the job/transformation being analyzed
   * @param node               the {@link IMetaverseNode} representing the job/transformation being analyzed
   * @param documentPath       the full file path of the job/transformation being analyzed
   * @return the {@link IMetaverseNode} representing the job/transformation being analyzed
   * @throws MetaverseAnalyzerException
   */
  IMetaverseNode analyze(
    final IComponentDescriptor documentDescriptor, final AbstractMeta meta, final IMetaverseNode node,
    final String documentPath ) throws MetaverseAnalyzerException;

}

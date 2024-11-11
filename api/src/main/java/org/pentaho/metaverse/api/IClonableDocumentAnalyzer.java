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

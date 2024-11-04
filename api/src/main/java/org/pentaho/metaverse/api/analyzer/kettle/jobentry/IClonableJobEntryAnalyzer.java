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


package org.pentaho.metaverse.api.analyzer.kettle.jobentry;

import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.metaverse.api.IClonableDocumentAnalyzer;
import org.pentaho.metaverse.api.IComponentDescriptor;

public interface IClonableJobEntryAnalyzer<S, T extends JobEntryInterface> extends IJobEntryAnalyzer<S, T> {

  /**
   * Clones this {@link IClonableJobEntryAnalyzer}.
   *
   * @return a clone of this {@link IClonableJobEntryAnalyzer}
   */
  IClonableJobEntryAnalyzer cloneAnalyzer();

  /**
   * Sets the {@link IClonableDocumentAnalyzer} associated with this analyzer.
   *
   * @param parentTransformationAnalyser the {@link IClonableDocumentAnalyzer} associated with this analyzer
   */
  void setDocumentAnalyzer( final IClonableDocumentAnalyzer parentTransformationAnalyser );

  /**
   * Returns the {@link IClonableDocumentAnalyzer} associated with this analyzer.
   * @return the {@link IClonableDocumentAnalyzer} associated with this analyzer
   */
  IClonableDocumentAnalyzer getDocumentAnalyzer();

  /**
   * Sets the {@link IComponentDescriptor} associated with this analyzer.
   *
   * @param documentDescriptor the {@link IComponentDescriptor} associated with this analyzer
   */
  void setDocumentDescriptor( final IComponentDescriptor documentDescriptor );

  /**
   * Returns the {@link IComponentDescriptor} associated with this analyzer.
   * @return the {@link IComponentDescriptor} associated with this analyzer
   */
  IComponentDescriptor getDocumentDescriptor();

  /**
   * Sets the full path of the document (transformation or job) containing the step associated with this analyzer.
   *
   * @param documentPath the full path of the document (transformation or job) containing the step associated with this
   *                     analyzer
   */
  void setDocumentPath( final String documentPath );
}

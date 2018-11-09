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

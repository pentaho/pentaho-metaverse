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

package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.model.kettle.IFieldMapping;

import java.util.Map;
import java.util.Set;

/**
 * IStepFieldMapper interface is for classes that can build a field mapping for a specific kind of PDI Step
 */
public interface IFieldLineageMetadataProvider<T extends BaseStepMeta> {

  /**
   * Get all of the ComponentDerivationRecord's defined by a step
   * @param meta
   * @return
   * @throws MetaverseAnalyzerException
   */
  public Set<ComponentDerivationRecord> getChangeRecords( T meta ) throws MetaverseAnalyzerException;

  /**
   * Get the mappings of input fields to output fields for a step
   * @param meta
   * @return
   * @throws MetaverseAnalyzerException
   */
  @Deprecated
  public Set<IFieldMapping> getFieldMappings( T meta ) throws MetaverseAnalyzerException;

  /**
   * Get RowMetaInterface(s) of Input step(s).
   * @param meta
   * @return Map of input step name to the RowMetaInterface coming from it
   */
  public Map<String, RowMetaInterface> getInputFields( T meta );

  /**
   * Get the RowMetaInterface for the output of the step
   * @param meta
   * @return
   */
  @Deprecated
  public RowMetaInterface getOutputFields( T meta );

}

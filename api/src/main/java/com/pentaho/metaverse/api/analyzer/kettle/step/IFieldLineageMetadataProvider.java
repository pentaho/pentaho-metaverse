/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.metaverse.api.analyzer.kettle.step;

import com.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;

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
  public RowMetaInterface getOutputFields( T meta );

}

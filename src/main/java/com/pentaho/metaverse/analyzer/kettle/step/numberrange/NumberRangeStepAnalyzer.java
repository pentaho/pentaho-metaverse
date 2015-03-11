/*!
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

package com.pentaho.metaverse.analyzer.kettle.step.numberrange;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;
import com.pentaho.metaverse.api.model.kettle.FieldMapping;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.numberrange.NumberRangeMeta;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.HashSet;
import java.util.Set;

/**
 * The SelectValuesStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the
 * fields operated on by Select Values steps.
 */
public class NumberRangeStepAnalyzer extends BaseStepAnalyzer<NumberRangeMeta> {

  /**
   * Analyzes Number Range steps to determine the various operations performed on fields and their data
   *
   * @see org.pentaho.platform.api.metaverse.IAnalyzer#analyze(org.pentaho.platform.api.metaverse.IComponentDescriptor, java.lang.Object)
   */
  @Override
  public IMetaverseNode analyze(
    IComponentDescriptor descriptor, NumberRangeMeta numberRangeMeta ) throws MetaverseAnalyzerException {

    // Do common analysis for all steps
    super.analyze( descriptor, numberRangeMeta );

    String inputFieldName = numberRangeMeta.getInputField();
    String outputFieldName = numberRangeMeta.getOutputField();

    if ( inputFieldName != null && outputFieldName != null ) {
      // We can't use our own descriptor here, we need to get the descriptor for the origin step
      IMetaverseNode inputFieldNode =
        createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, inputFieldName ) );

      // Not sure if we need a new node or not, but the builder will take care of it, so just create a node
      // so we can add the "derives" link
      IComponentDescriptor outputFieldDescriptor = getStepFieldOriginDescriptor( descriptor, outputFieldName );
      IMetaverseNode outputFieldNode = createNodeFromDescriptor( outputFieldDescriptor );

      metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, inputFieldNode );
      metaverseBuilder.addLink( inputFieldNode, DictionaryConst.LINK_DERIVES, outputFieldNode );
    }
    return rootNode;
  }

  /**
   * Provide field mappings that occur in this step.
   *
   * @param meta The step metadata
   * @return a set of field mappings (input field -> output field)
   * @throws org.pentaho.platform.api.metaverse.MetaverseAnalyzerException
   */
  @Override
  public Set<IFieldMapping> getFieldMappings( NumberRangeMeta meta ) throws MetaverseAnalyzerException {
    Set<IFieldMapping> fieldMappings = new HashSet<IFieldMapping>();
    fieldMappings.add( new FieldMapping( meta.getInputField(), meta.getOutputField() ) );
    fieldMappings.addAll( getPassthruFieldMappings( meta ) );
    return fieldMappings;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( NumberRangeMeta.class );
      }
    };
  }
}

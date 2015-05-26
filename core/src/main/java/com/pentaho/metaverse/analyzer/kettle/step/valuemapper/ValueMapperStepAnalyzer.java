/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.analyzer.kettle.step.valuemapper;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.ChangeType;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.StepField;
import com.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import com.pentaho.metaverse.api.model.Operation;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.valuemapper.ValueMapperMeta;

import java.util.HashSet;
import java.util.Set;


/**
 * The ValueMapperStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the fields
 * operated on by Value Mapper steps.
 */
public class ValueMapperStepAnalyzer extends StepAnalyzer<ValueMapperMeta> {

  @Override
  protected void customAnalyze( ValueMapperMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    rootNode.setProperty( "defaultValue", meta.getNonMatchDefault() );
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( ValueMapperMeta valueMapperMeta )
    throws MetaverseAnalyzerException {

    final String fieldToUse = valueMapperMeta.getFieldToUse();
    final String targetField = valueMapperMeta.getTargetField() == null ? fieldToUse : valueMapperMeta.getTargetField();
    final String[] sourceValues = valueMapperMeta.getSourceValue();
    final String[] targetValues = valueMapperMeta.getTargetValue();

    Set<ComponentDerivationRecord> changeRecords = new HashSet<>( 1 );
    final ComponentDerivationRecord changeRecord =
      new ComponentDerivationRecord( fieldToUse, targetField, ChangeType.DATA );

    for ( int i = 0; i < sourceValues.length; i++ ) {
      String mapping = sourceValues[i] + " -> " + targetValues[i];

      changeRecord.addOperation(
        new Operation( Operation.MAPPING_CATEGORY, ChangeType.DATA,
          DictionaryConst.PROPERTY_TRANSFORMS, mapping ) );
    }

    changeRecords.add( changeRecord );
    return changeRecords;
  }

  @Override
  protected Set<StepField> getUsedFields( ValueMapperMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();
    usedFields.addAll( createStepFields( meta.getFieldToUse(), getInputs() ) );
    return usedFields;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( ValueMapperMeta.class );
      }
    };
  }

}

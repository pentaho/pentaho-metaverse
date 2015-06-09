/*!
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

package com.pentaho.metaverse.analyzer.kettle.step.numberrange;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.model.Operation;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.numberrange.NumberRangeMeta;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.di.trans.steps.numberrange.NumberRangeRule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The NumberRangeStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the
 * fields operated on by Number Range steps.
 */
public class NumberRangeStepAnalyzer extends StepAnalyzer<NumberRangeMeta> {

  @Override
  protected Set<StepField> getUsedFields( NumberRangeMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();
    usedFields.addAll( createStepFields( meta.getInputField(), getInputs() ) );
    return usedFields;
  }

  @Override
  protected void customAnalyze( NumberRangeMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {

  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( final NumberRangeMeta meta )
    throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changeRecords = new HashSet<>();
    ComponentDerivationRecord changeRecord =
      new ComponentDerivationRecord( meta.getInputField(), meta.getOutputField(), ChangeType.DATA );
    List<NumberRangeRule> rules = meta.getRules();
    if ( rules != null ) {
      for ( NumberRangeRule rule : rules ) {
        changeRecord.addOperation( new Operation( Operation.MAPPING_CATEGORY, ChangeType.DATA,
          DictionaryConst.PROPERTY_TRANSFORMS,
          rule.getLowerBound() + " <= " + meta.getInputField() + " <= " + rule.getUpperBound()
            + " -> " + rule.getValue() ) );
      }
    }
    changeRecords.add( changeRecord );
    return changeRecords;
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

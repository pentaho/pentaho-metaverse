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

package org.pentaho.metaverse.analyzer.kettle.step.numberrange;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.numberrange.NumberRangeMeta;
import org.pentaho.di.trans.steps.numberrange.NumberRangeRule;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.model.Operation;

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

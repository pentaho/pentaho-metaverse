/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.calculator;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.model.Operation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalculatorStepAnalyzer extends StepAnalyzer<CalculatorMeta> {

  @Override
  protected void customAnalyze( CalculatorMeta meta, IMetaverseNode rootNode ) {
    // nothing custom to be done. The other overrides provide all that is needed
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> set = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    set.add( CalculatorMeta.class );
    return set;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( final CalculatorMeta calculatorMeta )
    throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>();
    for ( CalculatorMetaFunction function : calculatorMeta.getCalculation() ) {
      Set<ComponentDerivationRecord> changeRecord = buildChangeRecord( function );
      changeRecords.addAll( changeRecord );
    }
    return changeRecords;
  }

  @Override
  protected Set<StepField> getUsedFields( CalculatorMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();

    for ( CalculatorMetaFunction function : meta.getCalculation() ) {
      if ( StringUtils.isNotEmpty( function.getFieldA() ) ) {
        usedFields.addAll( createStepFields( function.getFieldA(), getInputs() ) );
      }
      if ( StringUtils.isNotEmpty( function.getFieldB() ) ) {
        usedFields.addAll( createStepFields( function.getFieldB(), getInputs() ) );
      }
      if ( StringUtils.isNotEmpty( function.getFieldC() ) ) {
        usedFields.addAll( createStepFields( function.getFieldC(), getInputs() ) );
      }
    }

    return usedFields;
  }

  protected Set<ComponentDerivationRecord> buildChangeRecord( final CalculatorMetaFunction function ) {

    Set<ComponentDerivationRecord> changes = new HashSet<>();

    String fieldA = function.getFieldA();
    String fieldB = function.getFieldB();
    String fieldC = function.getFieldC();
    String inputFields =
      ( fieldA != null ? fieldA + ", " : "" ) + ( fieldB != null ? fieldB + ", " : "" )
        + ( fieldC != null ? fieldC + ", " : "" );

    List<String> fields = new ArrayList<>();
    if ( fieldA != null ) {
      fields.add( fieldA );
    }

    if ( fieldB != null ) {
      fields.add( fieldB );
    }

    if ( fieldC != null ) {
      fields.add( fieldC );
    }

    for ( String field : fields ) {
      final ComponentDerivationRecord changeRecord =
        new ComponentDerivationRecord( field, function.getFieldName(), ChangeType.DATA );

      changeRecord.addOperation(
        new Operation( Operation.CALC_CATEGORY, ChangeType.DATA,
          DictionaryConst.PROPERTY_TRANSFORMS, inputFields + "using " + function.getCalcTypeDesc()
          + " -> " + function.getFieldName() ) );

      changes.add( changeRecord );

    }

    return changes;
  }

  @Override
  public IClonableStepAnalyzer newInstance() {
    return new CalculatorStepAnalyzer();
  }

}

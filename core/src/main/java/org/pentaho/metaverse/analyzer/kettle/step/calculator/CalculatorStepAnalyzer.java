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

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

package com.pentaho.metaverse.analyzer.kettle.step.calculator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.pentaho.metaverse.api.model.Operation;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.ChangeType;
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;

public class CalculatorStepAnalyzer extends BaseStepAnalyzer<CalculatorMeta> {
  private IComponentDescriptor descriptor;

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> set = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    set.add( CalculatorMeta.class );
    return set;
  }

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, CalculatorMeta calculatorMeta )
    throws MetaverseAnalyzerException {
    IMetaverseNode node = super.analyze( descriptor, calculatorMeta );
    this.descriptor = descriptor;
    getChangeRecords( calculatorMeta );
    return node;
  }

  protected ComponentDerivationRecord buildChangeRecord( final CalculatorMetaFunction function ) {

    final ComponentDerivationRecord changeRecord =
      new ComponentDerivationRecord( function.getFieldName(), ChangeType.DATA );
    String fieldA = function.getFieldA();
    String fieldB = function.getFieldB();
    String fieldC = function.getFieldC();
    String inputFields =
      ( fieldA != null ? fieldA + ", " : "" ) + ( fieldB != null ? fieldB + ", " : "" )
        + ( fieldC != null ? fieldC + ", " : "" );
    changeRecord.addOperation(
      new Operation( Operation.CALC_CATEGORY, ChangeType.DATA,
        DictionaryConst.PROPERTY_TRANSFORMS, inputFields + "using " + function.getCalcTypeDesc()
        + " -> " + function.getFieldName() ) );

    List<String> fields = new ArrayList<String>();
    fields.add( fieldA );
    fields.add( fieldB );
    fields.add( fieldC );
    for ( int i = 0; i < fields.size(); i++ ) {
      String fieldName = fields.get( i );
      if ( fieldName != null ) {
        IMetaverseNode fieldNode = createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, fieldName ) );
        IMetaverseNode newFieldNode = processFieldChangeRecord( descriptor, fieldNode, changeRecord );

        RowMetaInterface rowMetaInterface = prevFields.get( prevStepNames[0] );
        ValueMetaInterface inputFieldValueMeta = rowMetaInterface.searchValueMeta( fieldName );
        if ( inputFieldValueMeta != null ) {
          // this field is not created by this step, but it is used by it. add the link
          metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, fieldNode );
        }
        if ( newFieldNode != null ) {
          newFieldNode.setProperty( DictionaryConst.PROPERTY_KETTLE_TYPE, inputFieldValueMeta != null
            ? inputFieldValueMeta.getTypeDesc() : fieldName + " unknown type" );
          metaverseBuilder.addNode( newFieldNode );
          metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_CREATES, newFieldNode );
          if ( function.isRemovedFromResult() ) {
            metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, newFieldNode );
            metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_DELETES, newFieldNode );
          }
        }
      }
    }

    return changeRecord;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( final CalculatorMeta calculatorMeta )
    throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>();
    for ( CalculatorMetaFunction function : calculatorMeta.getCalculation() ) {
      ComponentDerivationRecord changeRecord = buildChangeRecord( function );
      changeRecords.add( changeRecord );
    }
    return changeRecords;
  }
}

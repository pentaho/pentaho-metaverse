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

package com.pentaho.metaverse.analyzer.kettle.step.stringoperations;

import java.util.HashSet;
import java.util.Set;

import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.stringoperations.StringOperationsMeta;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.model.Operation;

public class StringOperationsStepAnalyzer extends StepAnalyzer<StringOperationsMeta> {

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> set = new HashSet<>( 1 );
    set.add( StringOperationsMeta.class );
    return set;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( final StringOperationsMeta stringOperationsMeta )
    throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changeRecords = new HashSet<>();
    for ( int i = 0; i < stringOperationsMeta.getFieldInStream().length; i++ ) {
      String fieldInString = stringOperationsMeta.getFieldInStream()[i];
      String fieldOutString = stringOperationsMeta.getFieldOutStream()[i];
      if ( Const.isEmpty( fieldOutString ) ) {
        fieldOutString = fieldInString;
      }
      final ComponentDerivationRecord changeRecord = new ComponentDerivationRecord( fieldInString, fieldOutString, ChangeType.DATA );
      String trimTypeDescription = StringOperationsMeta.getTrimTypeDesc( stringOperationsMeta.getTrimType()[i] );
      String lowerUpperDescription = StringOperationsMeta.getLowerUpperDesc( stringOperationsMeta.getLowerUpper()[i] );
      String initCapDescription = StringOperationsMeta.getInitCapDesc( stringOperationsMeta.getInitCap()[i] );
      String digitsDescription = StringOperationsMeta.getDigitsDesc( stringOperationsMeta.getDigits()[i] );
      String maskXMLDescription = StringOperationsMeta.getMaskXMLDesc( stringOperationsMeta.getMaskXML()[i] );
      String paddingDescription = StringOperationsMeta.getPaddingDesc( stringOperationsMeta.getPaddingType()[i] );
      String specialCharactersDescription =
        StringOperationsMeta.getRemoveSpecialCharactersDesc( stringOperationsMeta.getRemoveSpecialCharacters()[i] );

      String changeOperation = fieldOutString;
      changeOperation += " { trim = [ " + trimTypeDescription + " ] && ";
      changeOperation += "lower/upper = [ " + lowerUpperDescription + " ] && ";
      changeOperation +=
        "padding = [ "
          + paddingDescription
          + ( stringOperationsMeta.getPaddingType()[i] == StringOperationsMeta.PADDING_NONE ? "" : ", "
          + stringOperationsMeta.getPadChar()[i] ) + ", " + stringOperationsMeta.getPadLen()[i]
          + " ] && ";
      changeOperation += "cap = [ " + initCapDescription + " ] && ";
      changeOperation += "maskXML = [ " + maskXMLDescription + " ] && ";
      changeOperation += "digits = [ " + digitsDescription + " ] && ";
      changeOperation += "remove = [ " + specialCharactersDescription + " ] } -> ";
      changeOperation += fieldOutString;

      changeRecord.addOperation( new Operation( Operation.CALC_CATEGORY, ChangeType.DATA,
        DictionaryConst.PROPERTY_TRANSFORMS, changeOperation ) );
      changeRecords.add( changeRecord );
    }
    return changeRecords;
  }

  @Override
  protected Set<StepField> getUsedFields( StringOperationsMeta meta ) {
    HashSet<StepField> usedFields = new HashSet<>();
    for ( String fieldInString : meta.getFieldInStream() ) {
      usedFields.addAll( createStepFields( fieldInString, getInputs() ) );
    }
    return usedFields;
  }

  /**
   * Determines if a field is considered a passthrough field or not. In this case, if we're not using the field, it's
   * a passthrough. If we are using the field, then it is only a passthrough if we're renaming the field on which we
   * perform the operation(s).
   *
   * @param originalFieldName The name of the incoming field
   * @return true if this field is a passthrough (i.e. no operations are performed on it), false otherwise
   */
  @Override
  protected boolean isPassthrough( StepField originalFieldName ) {
    String[] inFields = baseStepMeta.getFieldInStream();
    String origFieldName = originalFieldName.getFieldName();
    for ( int i = 0; i < inFields.length; i++ ) {
      if ( inFields[i].equals( origFieldName ) && Const.isEmpty( baseStepMeta.getFieldOutStream()[i] ) ) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected void customAnalyze( StringOperationsMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    // Nothing to do here
  }

  // ******** Start - Used to aid in unit testing **********
  protected void setStepMeta( StringOperationsMeta meta ) {
    this.baseStepMeta = meta;
  }
  // ******** End - Used to aid in unit testing **********

}

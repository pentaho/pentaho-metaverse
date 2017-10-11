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

package org.pentaho.metaverse.analyzer.kettle.step.stringoperations;

import org.pentaho.di.core.Const;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.stringoperations.StringOperationsMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.model.Operation;

import java.util.HashSet;
import java.util.Set;

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

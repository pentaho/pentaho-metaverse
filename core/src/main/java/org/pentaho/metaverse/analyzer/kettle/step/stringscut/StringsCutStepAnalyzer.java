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

package org.pentaho.metaverse.analyzer.kettle.step.stringscut;

import org.pentaho.di.core.Const;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.stringcut.StringCutMeta;
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

public class StringsCutStepAnalyzer extends StepAnalyzer<StringCutMeta> {

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> set = new HashSet<>( 1 );
    set.add( StringCutMeta.class );
    return set;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( StringCutMeta meta ) throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changeRecords = new HashSet<>();
    for ( int i = 0; i < meta.getFieldInStream().length; i++ ) {
      String fieldInString = meta.getFieldInStream()[i];
      String fieldOutString = meta.getFieldOutStream()[i];
      if ( fieldOutString == null || fieldOutString.length() < 1 ) {
        fieldOutString = fieldInString;
      }

      ComponentDerivationRecord changeRecord =
        new ComponentDerivationRecord( fieldInString, fieldOutString, ChangeType.DATA );
      String changeOperation =
        fieldInString + " -> [ substring [ " + meta.getCutFrom()[i] + ", "
          + meta.getCutTo()[i] + " ] ] -> " + fieldOutString;
      changeRecord.addOperation( new Operation( Operation.CALC_CATEGORY, ChangeType.DATA,
        DictionaryConst.PROPERTY_TRANSFORMS, changeOperation ) );
      changeRecords.add( changeRecord );
    }
    return changeRecords;
  }

  @Override
  protected Set<StepField> getUsedFields( StringCutMeta meta ) {
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
  protected void customAnalyze( StringCutMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    // Nothing to do here
  }

  // ******** Start - Used to aid in unit testing **********
  protected void setStepMeta( StringCutMeta meta ) {
    this.baseStepMeta = meta;
  }
  // ******** End - Used to aid in unit testing **********
}

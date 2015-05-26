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

package com.pentaho.metaverse.analyzer.kettle.step.stringscut;

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.stringcut.StringCutMeta;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.ChangeType;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.StepField;
import com.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import com.pentaho.metaverse.api.model.Operation;

public class StringsCutStepAnalyzer extends StepAnalyzer<StringCutMeta> {

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> set = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    set.add( StringCutMeta.class );
    return set;
  }

  private String getChangeOperation( final String fieldInString, final String fieldOutString, final String from,
      final String to ) {
    String changeOperation = fieldInString + " -> [ substring [ ";
    changeOperation += from + ", ";
    changeOperation += to + " ] ] -> ";
    changeOperation += fieldOutString;

    return changeOperation;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( StringCutMeta meta ) throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>();
    for ( int i = 0; i < meta.getFieldInStream().length; i++ ) {
      String fieldInString = meta.getFieldInStream()[i];
      String fieldOutString = meta.getFieldOutStream()[i];
      if ( fieldOutString == null || fieldOutString.length() < 1 ) {
        fieldOutString = fieldInString;
      }

      ComponentDerivationRecord changeRecord = new ComponentDerivationRecord( fieldOutString, ChangeType.DATA );
      String changeOperation =
          getChangeOperation( fieldInString, fieldOutString, meta.getCutFrom()[i], meta.getCutTo()[i] );
      changeRecord.addOperation( new Operation( Operation.CALC_CATEGORY, ChangeType.DATA,
          DictionaryConst.PROPERTY_TRANSFORMS, changeOperation ) );
      changeRecords.add( changeRecord );

      if ( !fieldOutString.equals( fieldInString ) ) {
        changeRecord = new ComponentDerivationRecord( fieldInString, ChangeType.DATA );
        changeOperation = getChangeOperation( fieldInString, fieldOutString, meta.getCutFrom()[i], meta.getCutTo()[i] );
        changeRecord.setChangedEntityName( fieldOutString );
        changeRecord.addOperation( new Operation( Operation.CALC_CATEGORY, ChangeType.DATA,
            DictionaryConst.PROPERTY_TRANSFORMS, changeOperation ) );
        changeRecords.add( changeRecord );
      }
    }
    return changeRecords;
  }

  @Override
  protected Set<StepField> getUsedFields( StringCutMeta meta ) {
    HashSet<StepField> usedFields = new HashSet<StepField>();
    for ( String fieldInString : meta.getFieldInStream() ) {
      usedFields.addAll( createStepFields( fieldInString, getInputs() ) );
    }
    return usedFields;
  }

  @Override
  protected void customAnalyze( StringCutMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    // TODO Auto-generated method stub

  }

}

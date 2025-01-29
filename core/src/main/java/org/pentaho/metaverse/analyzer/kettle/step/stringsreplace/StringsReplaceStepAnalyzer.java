/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.step.stringsreplace;

import org.pentaho.di.core.Const;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.replacestring.ReplaceStringMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.model.Operation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StringsReplaceStepAnalyzer extends StepAnalyzer<ReplaceStringMeta> {

  private Map<String, Integer> renameIndex = new HashMap<String, Integer>();

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> set = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    set.add( ReplaceStringMeta.class );
    return set;
  }


  private ComponentDerivationRecord buildChangeRecord( final ReplaceStringMeta stringsReplaceMeta, final int index )
    throws MetaverseAnalyzerException {

    String fieldInString = stringsReplaceMeta.getFieldInStream()[index];
    String fieldOutString = stringsReplaceMeta.getFieldOutStream()[index];
    if ( containsField( fieldOutString ) ) {
      Integer nameIdx = renameIndex.get( fieldOutString );
      renameIndex.put( fieldOutString, ( nameIdx == null ? 1 : nameIdx + 1 ) );
      fieldOutString += "_" + renameIndex.get( fieldOutString );
    }
    if ( fieldOutString == null || fieldOutString.length() < 1 ) {
      fieldOutString = fieldInString;
    }
    final ComponentDerivationRecord changeRecord =
      new ComponentDerivationRecord( fieldInString, fieldOutString, ChangeType.DATA );
    final String fieldReplaceString = stringsReplaceMeta.getFieldReplaceByString()[index];
    String changeOperation = fieldInString + " -> [ replace [ ";
    changeOperation += stringsReplaceMeta.getReplaceString()[index] + " with ";
    if ( fieldReplaceString != null && fieldReplaceString.length() > 0 ) {
      changeOperation += fieldReplaceString;
    } else {
      changeOperation += stringsReplaceMeta.getReplaceByString()[0];
    }
    changeOperation += " ] ] -> " + fieldOutString;

    changeRecord.addOperation( new Operation( Operation.CALC_CATEGORY, ChangeType.DATA,
      DictionaryConst.PROPERTY_TRANSFORMS, changeOperation ) );

    return changeRecord;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( final ReplaceStringMeta stringsReplaceMeta )
    throws MetaverseAnalyzerException {
    renameIndex.clear();
    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>();
    for ( int i = 0; i < stringsReplaceMeta.getFieldInStream().length; i++ ) {
      ComponentDerivationRecord changeRecord = buildChangeRecord( stringsReplaceMeta, i );
      changeRecords.add( changeRecord );
    }
    return changeRecords;
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
  protected Set<StepField> getUsedFields( ReplaceStringMeta meta ) {
    HashSet<StepField> usedFields = new HashSet<>();
    for ( String fieldInString : meta.getFieldInStream() ) {
      usedFields.addAll( createStepFields( fieldInString, getInputs() ) );
    }
    for ( String replaceByField : meta.getFieldReplaceByString() ) {
      if ( !Const.isEmpty( replaceByField ) ) {
        usedFields.addAll( createStepFields( replaceByField, getInputs() ) );
      }
    }
    return usedFields;
  }

  @Override
  protected void customAnalyze( ReplaceStringMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    // Nothing to do here
  }

  private boolean containsField( String field ) {
    return ( !Const.isEmpty( field ) && !Const.isEmpty( getInputs().findNodes( field ) ) );
  }

  // ******** Start - Used to aid in unit testing **********
  protected void setStepMeta( ReplaceStringMeta meta ) {
    this.baseStepMeta = meta;
  }
  // ******** End - Used to aid in unit testing **********

}

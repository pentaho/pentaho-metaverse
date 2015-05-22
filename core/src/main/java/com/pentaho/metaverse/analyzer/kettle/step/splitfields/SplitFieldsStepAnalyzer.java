/*
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
 *
 */

package com.pentaho.metaverse.analyzer.kettle.step.splitfields;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.ChangeType;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.StepField;
import com.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import com.pentaho.metaverse.api.model.Operation;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.fieldsplitter.FieldSplitterMeta;

import java.util.HashSet;
import java.util.Set;

/**
 * Step analyzer for Field Splitter step
 */
public class SplitFieldsStepAnalyzer extends StepAnalyzer<FieldSplitterMeta> {

  @Override
  protected Set<StepField> getUsedFields( FieldSplitterMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();
    usedFields.addAll( createStepFields( meta.getSplitField(), getInputs() ) );
    return usedFields;
  }

  @Override
  protected void customAnalyze( FieldSplitterMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    rootNode.setProperty( DictionaryConst.PROPERTY_DELIMITER, meta.getDelimiter() );
    rootNode.setProperty( DictionaryConst.PROPERTY_ENCLOSURE, meta.getEnclosure() );
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( FieldSplitterMeta meta )
    throws MetaverseAnalyzerException {

    String originalField = meta.getSplitField();

    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>();

    String[] fieldNames = meta.getFieldName();

    if ( !Const.isEmpty( fieldNames ) ) {
      for ( int i = 0; i < meta.getFieldName().length; i++ ) {
        ComponentDerivationRecord cdr = new ComponentDerivationRecord( originalField, fieldNames[i], ChangeType.DATA );
        cdr.addOperation( new Operation( Operation.MAPPING_CATEGORY, ChangeType.DATA, fieldNames[i],
          "Token " + i + " of split string" ) );
        changeRecords.add( cdr );
      }
    }

    return changeRecords;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    HashSet<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<Class<? extends BaseStepMeta>>();
    supportedSteps.add( FieldSplitterMeta.class );
    return supportedSteps;
  }

}

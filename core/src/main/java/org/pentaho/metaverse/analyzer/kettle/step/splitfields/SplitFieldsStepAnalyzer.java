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

package org.pentaho.metaverse.analyzer.kettle.step.splitfields;

import org.pentaho.di.core.Const;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.fieldsplitter.FieldSplitterMeta;
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

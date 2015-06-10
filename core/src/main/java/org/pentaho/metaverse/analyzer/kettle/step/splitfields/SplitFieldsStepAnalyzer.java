/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

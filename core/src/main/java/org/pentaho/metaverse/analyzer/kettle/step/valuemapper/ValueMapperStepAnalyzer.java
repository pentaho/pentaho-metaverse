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

package org.pentaho.metaverse.analyzer.kettle.step.valuemapper;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.valuemapper.ValueMapperMeta;
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
 * The ValueMapperStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the fields
 * operated on by Value Mapper steps.
 */
public class ValueMapperStepAnalyzer extends StepAnalyzer<ValueMapperMeta> {

  @Override
  protected void customAnalyze( ValueMapperMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    rootNode.setProperty( "defaultValue", meta.getNonMatchDefault() );
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( ValueMapperMeta valueMapperMeta )
    throws MetaverseAnalyzerException {

    final String fieldToUse = valueMapperMeta.getFieldToUse();
    final String targetField = valueMapperMeta.getTargetField() == null ? fieldToUse : valueMapperMeta.getTargetField();
    final String[] sourceValues = valueMapperMeta.getSourceValue();
    final String[] targetValues = valueMapperMeta.getTargetValue();

    Set<ComponentDerivationRecord> changeRecords = new HashSet<>( 1 );
    final ComponentDerivationRecord changeRecord =
      new ComponentDerivationRecord( fieldToUse, targetField, ChangeType.DATA );

    for ( int i = 0; i < sourceValues.length; i++ ) {
      String mapping = sourceValues[i] + " -> " + targetValues[i];

      changeRecord.addOperation(
        new Operation( Operation.MAPPING_CATEGORY, ChangeType.DATA,
          DictionaryConst.PROPERTY_TRANSFORMS, mapping ) );
    }

    changeRecords.add( changeRecord );
    return changeRecords;
  }

  @Override
  protected Set<StepField> getUsedFields( ValueMapperMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();
    usedFields.addAll( createStepFields( meta.getFieldToUse(), getInputs() ) );
    return usedFields;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( ValueMapperMeta.class );
      }
    };
  }

}

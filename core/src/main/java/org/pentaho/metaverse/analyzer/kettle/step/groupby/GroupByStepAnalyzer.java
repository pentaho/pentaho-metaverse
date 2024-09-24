/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.groupby;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.groupby.GroupByMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.model.Operation;

import java.util.HashSet;
import java.util.Set;

/**
 * The GroupByStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the fields
 * operated on by Group By steps.
 */
public class GroupByStepAnalyzer extends StepAnalyzer<GroupByMeta> {

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( final GroupByMeta groupByMeta )
    throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changeRecords = new HashSet<>();
    for ( int i = 0; i < groupByMeta.getSubjectField().length; i++ ) {
      ComponentDerivationRecord changeRecord = buildChangeRecord( groupByMeta.getSubjectField()[i],
        groupByMeta.getAggregateField()[i],
        groupByMeta.getAggregateType()[i] );
      changeRecords.add( changeRecord );
    }
    return changeRecords;
  }

  private ComponentDerivationRecord buildChangeRecord( String subjectField, String aggregateField, int aggregateType ) {
    final ComponentDerivationRecord changeRecord = new ComponentDerivationRecord( subjectField,
      aggregateField, ChangeType.DATA );
    changeRecord.addOperation( new Operation( Operation.AGG_CATEGORY, ChangeType.DATA,
      DictionaryConst.PROPERTY_TRANSFORMS, subjectField + " using " + GroupByMeta.getTypeDesc( aggregateType )
      + " -> " + aggregateField ) );

    return changeRecord;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( GroupByMeta.class );
      }
    };
  }

  @Override protected Set<StepField> getUsedFields( GroupByMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();
    String[] groupFields = meta.getGroupField();
    for ( String groupField : groupFields ) {
      usedFields.addAll( createStepFields( groupField, getInputs() ) );
    }
    String[] subjectFields = meta.getSubjectField();
    for ( String subjectField : subjectFields ) {
      usedFields.addAll( createStepFields( subjectField, getInputs() ) );
    }
    return usedFields;
  }

  @Override protected void customAnalyze( GroupByMeta meta, IMetaverseNode rootNode )
    throws MetaverseAnalyzerException {
    // nothing custom to do
  }

  @Override
  public IClonableStepAnalyzer newInstance() {
    return new GroupByStepAnalyzer();
  }
}

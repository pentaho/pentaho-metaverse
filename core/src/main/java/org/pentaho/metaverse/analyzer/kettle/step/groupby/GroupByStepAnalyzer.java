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

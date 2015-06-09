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

package org.pentaho.metaverse.analyzer.kettle.step.groupby;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.groupby.GroupByMeta;
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
}

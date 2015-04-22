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

package com.pentaho.metaverse.analyzer.kettle.step.groupby;

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.groupby.GroupByMeta;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.ChangeType;
import com.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.api.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.api.model.Operation;

/**
 * The SelectValuesStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the fields
 * operated on by Select Values steps.
 */
public class GroupByStepAnalyzer extends BaseStepAnalyzer<GroupByMeta> {
  private IComponentDescriptor descriptor;

  /*
   * (non-Javadoc)
   * 
   * @see com.pentaho.metaverse.api.IAnalyzer#analyze(IComponentDescriptor,java.lang.Object)
   */
  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, GroupByMeta groupByMeta )
    throws MetaverseAnalyzerException {

    this.descriptor = descriptor;

    // Do common analysis for all steps
    IMetaverseNode node = super.analyze( descriptor, groupByMeta );

    // add "uses" links between the stream fields that are used to "group by"
    addGroupByUsesLinks( node, groupByMeta );

    getChangeRecords( groupByMeta );

    return rootNode;
  }

  protected void addGroupByUsesLinks( IMetaverseNode node, GroupByMeta groupByMeta ) {
    String[] groupFields = groupByMeta.getGroupField();
    for ( int i = 0; i < groupFields.length; i++ ) {
      String groupField = groupFields[ i ];
      IComponentDescriptor prevDescriptor = getPrevStepFieldOriginDescriptor( descriptor, groupField );
      IMetaverseNode groupFieldNode = createNodeFromDescriptor( prevDescriptor );
      metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, groupFieldNode );
    }
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( final GroupByMeta groupByMeta )
    throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>();
    for ( int i = 0; i < groupByMeta.getSubjectField().length; i++ ) {

      // If the aggregate field is the same name as a subject field, it is technically a new field but will not be
      // created by the base step analyzer. We can do this naively by running through all aggregate fields, checking to
      // see if its node already exists, and creating/adding it if it does not exist.
      if ( stepFields != null ) {
        ValueMetaInterface vmi = stepFields.searchValueMeta( groupByMeta.getAggregateField()[i] );
        createFieldNode( descriptor.getContext(), vmi );
      }

      // If the subject field name equals the aggregate field name, then the subject field is technically deleted and
      // the new one is created in the above code, so add a "deletes" relationship from the step to the subject field.
      if ( groupByMeta.getSubjectField()[i].equals( groupByMeta.getAggregateField()[i] ) ) {
        IMetaverseNode subjectFieldNode = createNodeFromDescriptor(
          this.getPrevStepFieldOriginDescriptor( descriptor, groupByMeta.getSubjectField()[i] ) );
        metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_DELETES, subjectFieldNode );
      }

      ComponentDerivationRecord changeRecord =
        buildChangeRecord( groupByMeta.getSubjectField()[i], groupByMeta.getAggregateField()[i], groupByMeta
          .getAggregateType()[i] );
      changeRecords.add( changeRecord );
    }
    return changeRecords;
  }

  private ComponentDerivationRecord buildChangeRecord( String subjectField, String aggregateField, int aggregateType ) {
    final ComponentDerivationRecord changeRecord = new ComponentDerivationRecord( aggregateField, ChangeType.DATA );
    changeRecord.addOperation( new Operation( Operation.AGG_CATEGORY, ChangeType.DATA,
      DictionaryConst.PROPERTY_TRANSFORMS, subjectField + " using " + GroupByMeta.getTypeDesc( aggregateType )
      + " -> " + aggregateField ) );
    IMetaverseNode subjectFieldNode =
      createNodeFromDescriptor( this.getPrevStepFieldOriginDescriptor( descriptor, subjectField ) );
    processFieldChangeRecord( descriptor, subjectFieldNode, changeRecord );
    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, subjectFieldNode );

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
}

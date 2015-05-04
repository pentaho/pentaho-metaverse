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

package com.pentaho.metaverse.analyzer.kettle.step.filterrows;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.ChangeType;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.api.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.api.model.Operation;
import org.pentaho.di.core.Condition;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;

import java.util.HashSet;
import java.util.Set;

public class FilterRowsStepAnalyzer extends BaseStepAnalyzer<FilterRowsMeta> {

  public static final String DATA_FLOW_CONDITION = "dataFlowCondition";

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, FilterRowsMeta stepMeta )
    throws MetaverseAnalyzerException {

    IMetaverseNode stepNode = super.analyze( descriptor, stepMeta );

    customAnalyze( descriptor, stepMeta, stepNode );

    return stepNode;
  }

  protected void customAnalyze( IComponentDescriptor descriptor, FilterRowsMeta stepMeta, IMetaverseNode stepNode ) {
    // add the filter condition as properties on the step node

    final Condition condition = stepMeta.getCondition();

    if ( condition != null ) {
      String filterCondition = condition.toString();
      Operation operation = new Operation( "filter", ChangeType.DATA_FLOW, DATA_FLOW_CONDITION, filterCondition );

      ComponentDerivationRecord changeRecord = new ComponentDerivationRecord( stepNode.getName(), ChangeType.DATA_FLOW );
      changeRecord.addOperation( operation );
      stepNode.setProperty( DictionaryConst.PROPERTY_OPERATIONS, changeRecord.toString() );

      // add uses links to all of the fields that are part of the filter condition
      for ( String usedField : condition.getUsedFields() ) {
        IComponentDescriptor fieldOriginDescriptor = getPrevStepFieldOriginDescriptor( descriptor, usedField );
        if ( fieldOriginDescriptor != null ) {
          IMetaverseNode nodeFromDescriptor = createNodeFromDescriptor( fieldOriginDescriptor );
          metaverseBuilder.addLink( stepNode, DictionaryConst.LINK_USES, nodeFromDescriptor );
        }
      }
    }

  }


  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<>();
    supportedSteps.add( FilterRowsMeta.class );
    return supportedSteps;
  }

}

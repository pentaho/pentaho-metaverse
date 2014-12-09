/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.valuemapper.ValueMapperMeta;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.HashSet;
import java.util.Set;

/**
 * User: RFellows Date: 9/16/14
 */
public class ValueMapperStepAnalyzer extends BaseStepAnalyzer<ValueMapperMeta> {

  @Override
  public IMetaverseNode analyze( final IMetaverseComponentDescriptor descriptor, final ValueMapperMeta valueMapperMeta )
    throws MetaverseAnalyzerException {

    final IMetaverseNode node = super.analyze( descriptor, valueMapperMeta );

    final String fieldToUse = valueMapperMeta.getFieldToUse();
    final boolean overwritesSourceField = Const.isEmpty( valueMapperMeta.getTargetField() );
    final String targetField = valueMapperMeta.getTargetField() == null ? fieldToUse : valueMapperMeta.getTargetField();

    final IMetaverseNode sourceFieldNode = createNodeFromDescriptor(
        getPrevStepFieldOriginDescriptor( descriptor, fieldToUse ) );

    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, sourceFieldNode );

    final Set<ComponentDerivationRecord> changeRecords = getChangeRecords( valueMapperMeta );

    ComponentDerivationRecord changeRecord = null;
    for ( ComponentDerivationRecord cr : changeRecords ) {
      changeRecord = cr;
      break;
    }

    // if no target field specified
    if ( overwritesSourceField ) {
      // add some data operation property to the source node
      sourceFieldNode.setProperty( DictionaryConst.PROPERTY_OPERATIONS, changeRecord.toString() );
      metaverseBuilder.updateNode( sourceFieldNode );
    } else {
      final IMetaverseComponentDescriptor desc = new MetaverseComponentDescriptor( targetField,
          DictionaryConst.NODE_TYPE_TRANS_FIELD, descriptor.getNamespace() );

      // Get the ValueMetaInterface for the input field, to determine if any of its metadata has changed
      final ValueMetaInterface inputFieldValueMeta = prevFields.searchValueMeta( fieldToUse );
      if ( inputFieldValueMeta == null ) {
        throw new MetaverseAnalyzerException( "Cannot determine type of field: " + fieldToUse );
      }

      final IMetaverseNode targetFieldNode = processFieldChangeRecord( desc, sourceFieldNode, changeRecord );
      targetFieldNode.setProperty( DictionaryConst.PROPERTY_KETTLE_TYPE, inputFieldValueMeta.getTypeDesc() );
    }

    return node;
  }

  protected ComponentDerivationRecord buildChangeRecord(
      final String fieldName, final String[] sourceValues, final String[] targetValues ) {

    final ComponentDerivationRecord changeRecord = new ComponentDerivationRecord( fieldName );
    for ( int i = 0; i < sourceValues.length; i++ ) {
      changeRecord.addOperand( DictionaryConst.PROPERTY_TRANSFORMS, sourceValues[i] + " -> " + targetValues[i] );
    }
    return changeRecord;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( ValueMapperMeta valueMapperMeta )
    throws MetaverseAnalyzerException {

    final String fieldToUse = valueMapperMeta.getFieldToUse();
    final String targetField = valueMapperMeta.getTargetField() == null ? fieldToUse : valueMapperMeta.getTargetField();
    final String[] sourceValues = valueMapperMeta.getSourceValue();
    final String[] targetValues = valueMapperMeta.getTargetValue();

    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>( 1 );
    final ComponentDerivationRecord changeRecord = buildChangeRecord( targetField, sourceValues, targetValues );

    changeRecords.add( changeRecord );
    return changeRecords;
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

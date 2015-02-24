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
 */

package com.pentaho.metaverse.analyzer.kettle.step.valuemapper;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.ChangeType;
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.api.model.Operation;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.impl.model.kettle.FieldMapping;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.valuemapper.ValueMapperMeta;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.HashSet;
import java.util.Set;


/**
 * The ValueMapperStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the fields
 * operated on by Value Mapper steps.
 */
public class ValueMapperStepAnalyzer extends BaseStepAnalyzer<ValueMapperMeta> {

  @Override
  public IMetaverseNode analyze( final IComponentDescriptor descriptor, final ValueMapperMeta valueMapperMeta )
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
      final IComponentDescriptor desc = new MetaverseComponentDescriptor( targetField,
        DictionaryConst.NODE_TYPE_TRANS_FIELD, descriptor.getNamespace() );

      // Get the ValueMetaInterface for the input field, to determine if any of its metadata has changed
      RowMetaInterface rowMetaInterface = prevFields.get( prevStepNames[0] );
      final ValueMetaInterface inputFieldValueMeta = rowMetaInterface.searchValueMeta( fieldToUse );
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

    final ComponentDerivationRecord changeRecord = new ComponentDerivationRecord( fieldName, ChangeType.DATA );
    Set<String> metadataChangedFields = new HashSet<String>();

    for ( int i = 0; i < sourceValues.length; i++ ) {
      metadataChangedFields.add( sourceValues[i] + " -> " + targetValues[i] );
    }
    changeRecord.addOperation(
      new Operation( Operation.MAPPING_CATEGORY, ChangeType.DATA,
        DictionaryConst.PROPERTY_TRANSFORMS, StringUtils.join( metadataChangedFields, "," ) ) );

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

  /**
   * Provide field mappings that occur in this step.
   *
   * @param meta The step metadata
   * @return a set of field mappings (input field -> output field)
   * @throws org.pentaho.platform.api.metaverse.MetaverseAnalyzerException
   */
  @Override
  public Set<IFieldMapping> getFieldMappings( ValueMapperMeta meta ) throws MetaverseAnalyzerException {
    Set<IFieldMapping> fieldMappings = new HashSet<IFieldMapping>();
    fieldMappings.add( new FieldMapping( meta.getFieldToUse(), meta.getTargetField() ) );
    fieldMappings.addAll( getPassthruFieldMappings( meta ) );
    return fieldMappings;
  }

}

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

package com.pentaho.metaverse.analyzer.kettle.step.stringoperations;

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.stringoperations.StringOperationsMeta;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.ChangeType;
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.model.Operation;
import com.pentaho.metaverse.api.model.kettle.FieldMapping;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;

public class StringOperationsStepAnalyzer extends BaseStepAnalyzer<StringOperationsMeta> {
  private IComponentDescriptor descriptor;

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> set = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    set.add( StringOperationsMeta.class );
    return set;
  }

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, StringOperationsMeta stringOperationsMeta )
    throws MetaverseAnalyzerException {
    IMetaverseNode node = super.analyze( descriptor, stringOperationsMeta );
    this.descriptor = descriptor;
    getChangeRecords( stringOperationsMeta );
    return node;
  }

  private ComponentDerivationRecord buildChangeRecord( final StringOperationsMeta stringOperationsMeta, final int index ) throws MetaverseAnalyzerException {

    String fieldInString = stringOperationsMeta.getFieldInStream()[index];
    String fieldOutString = stringOperationsMeta.getFieldOutStream()[index];
    if ( fieldOutString == null || fieldOutString.length() < 1 ) {
      fieldOutString = fieldInString;
    }
    final ComponentDerivationRecord changeRecord = new ComponentDerivationRecord( fieldOutString, ChangeType.DATA );
    String trimTypeDescription = StringOperationsMeta.getTrimTypeDesc( stringOperationsMeta.getTrimType()[index] );
    String lowerUpperDescription = StringOperationsMeta.getLowerUpperDesc( stringOperationsMeta.getLowerUpper()[index] );
    String initCapDescription = StringOperationsMeta.getInitCapDesc( stringOperationsMeta.getInitCap()[index] );
    String digitsDescription = StringOperationsMeta.getDigitsDesc( stringOperationsMeta.getDigits()[index] );
    String maskXMLDescription = StringOperationsMeta.getMaskXMLDesc( stringOperationsMeta.getMaskXML()[index] );
    String paddingDescription = StringOperationsMeta.getPaddingDesc( stringOperationsMeta.getPaddingType()[index] );
    String specialCharactersDescription =
        StringOperationsMeta.getRemoveSpecialCharactersDesc( stringOperationsMeta.getRemoveSpecialCharacters()[index] );

    String changeOperation = fieldOutString;
    changeOperation += " { trim = [ " + trimTypeDescription + " ] && ";
    changeOperation += "lower/upper = [ " + lowerUpperDescription + " ] && ";
    changeOperation +=
        "padding = [ "
            + paddingDescription
            + ( stringOperationsMeta.getPaddingType()[index] == StringOperationsMeta.PADDING_NONE ? "" : ", "
                + stringOperationsMeta.getPadChar()[index] ) + ", " + stringOperationsMeta.getPadLen()[index]
            + " ] && ";
    changeOperation += "cap = [ " + initCapDescription + " ] && ";
    changeOperation += "maskXML = [ " + maskXMLDescription + " ] && ";
    changeOperation += "digits = [ " + digitsDescription + " ] && ";
    changeOperation += "remove = [ " + specialCharactersDescription + " ] } -> ";
    changeOperation +=
        ( stringOperationsMeta.getFieldOutStream()[index] == null
            || stringOperationsMeta.getFieldOutStream()[index].length() < 1
            ? stringOperationsMeta.getFieldInStream()[index] : stringOperationsMeta.getFieldOutStream()[index] );

    changeRecord.addOperation( new Operation( Operation.CALC_CATEGORY, ChangeType.DATA,
        DictionaryConst.PROPERTY_TRANSFORMS, changeOperation ) );
    IMetaverseNode fieldNode = createNodeFromDescriptor( getStepFieldOriginDescriptor( descriptor, fieldInString ) );
    IMetaverseNode newFieldNode = processFieldChangeRecord( descriptor, fieldNode, changeRecord );
    RowMetaInterface rowMetaInterface = prevFields.get( prevStepNames[0] );
    ValueMetaInterface inputFieldValueMeta = rowMetaInterface.searchValueMeta( fieldInString );
    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, fieldNode );
    if ( !fieldOutString.equals( fieldInString ) ) {
      newFieldNode.setProperty( DictionaryConst.PROPERTY_KETTLE_TYPE, inputFieldValueMeta != null ? inputFieldValueMeta
          .getTypeDesc() : fieldInString + " unknown type" );
      metaverseBuilder.addNode( newFieldNode );
    }
    return changeRecord;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( final StringOperationsMeta stringOperationsMeta )
    throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>();
    for ( int i = 0; i < stringOperationsMeta.getFieldInStream().length; i++ ) {
      ComponentDerivationRecord changeRecord = buildChangeRecord( stringOperationsMeta, i );
      changeRecords.add( changeRecord );
    }
    return changeRecords;
  }

  /**
   * Provide field mappings that occur in this step.
   *
   * @param meta
   *          The step metadata
   * @return a set of field mappings (input field -> output field)
   * @throws com.pentaho.metaverse.api.MetaverseAnalyzerException
   */
  @Override
  public Set<IFieldMapping> getFieldMappings( StringOperationsMeta meta ) throws MetaverseAnalyzerException {
    Set<IFieldMapping> fieldMappings = new HashSet<IFieldMapping>();
    for ( int i = 0; i < meta.getFieldInStream().length; i++ ) {
      String fieldInString = meta.getFieldInStream()[i];
      String fieldOutString = meta.getFieldOutStream()[i];
      if ( fieldOutString != null && fieldOutString.length() > 0 ) {
        fieldMappings.add( new FieldMapping( fieldInString, fieldOutString ) );
      }
    }
    fieldMappings.addAll( getPassthruFieldMappings( meta ) );
    return fieldMappings;
  }

}

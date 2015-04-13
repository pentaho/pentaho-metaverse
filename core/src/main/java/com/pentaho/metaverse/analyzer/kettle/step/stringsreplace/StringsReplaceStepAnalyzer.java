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

package com.pentaho.metaverse.analyzer.kettle.step.stringsreplace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.replacestring.ReplaceStringMeta;

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

public class StringsReplaceStepAnalyzer extends BaseStepAnalyzer<ReplaceStringMeta> {
  private IComponentDescriptor descriptor;

  private Map<String, Integer> renameIndex = new HashMap<String, Integer>();

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> set = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    set.add( ReplaceStringMeta.class );
    return set;
  }

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, ReplaceStringMeta stringsReplaceMeta )
    throws MetaverseAnalyzerException {
    IMetaverseNode node = super.analyze( descriptor, stringsReplaceMeta );
    this.descriptor = descriptor;
    getChangeRecords( stringsReplaceMeta );
    return node;
  }

  private ComponentDerivationRecord buildChangeRecord( final ReplaceStringMeta stringsReplaceMeta, final int index )
    throws MetaverseAnalyzerException {

    String fieldInString = stringsReplaceMeta.getFieldInStream()[index];
    String fieldOutString = stringsReplaceMeta.getFieldOutStream()[index];
    if ( fieldInString.equals( fieldOutString ) ) {
      Integer nameIdx = renameIndex.get( fieldOutString );
      renameIndex.put( fieldOutString, ( nameIdx == null ? 1 : nameIdx++ ) );
      fieldOutString += "_" + renameIndex.get( fieldOutString );
    }
    if ( fieldOutString == null || fieldOutString.length() < 1 ) {
      fieldOutString = fieldInString;
    }
    final ComponentDerivationRecord changeRecord = new ComponentDerivationRecord( fieldOutString, ChangeType.DATA );
    final String fieldReplaceString = stringsReplaceMeta.getFieldReplaceByString()[index];
    String changeOperation = fieldInString + " -> [ replace [ ";
    changeOperation += stringsReplaceMeta.getReplaceString()[index] + " with ";
    if ( fieldReplaceString != null && fieldReplaceString.length() > 0 ) {
      changeOperation += fieldReplaceString;
    } else {
      changeOperation += stringsReplaceMeta.getReplaceByString()[0];
    }
    changeOperation += " ] ] -> " + fieldOutString;

    changeRecord.addOperation( new Operation( Operation.CALC_CATEGORY, ChangeType.DATA,
        DictionaryConst.PROPERTY_TRANSFORMS, changeOperation ) );
    IMetaverseNode fieldNode = createNodeFromDescriptor( getStepFieldOriginDescriptor( descriptor, fieldInString ) );
    IMetaverseNode newFieldNode = processFieldChangeRecord( descriptor, fieldNode, changeRecord );
    RowMetaInterface rowMetaInterface = prevFields.get( prevStepNames[0] );
    ValueMetaInterface inputFieldValueMeta = rowMetaInterface.searchValueMeta( fieldInString );
    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, fieldNode );
    if ( fieldReplaceString != null && fieldReplaceString.length() > 0 ) {
      IMetaverseNode replacementFieldNode =
          createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, fieldReplaceString ) );
      metaverseBuilder.addLink( replacementFieldNode, DictionaryConst.LINK_DERIVES, newFieldNode );
      metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, replacementFieldNode );
    }

    if ( !fieldOutString.equals( fieldInString ) ) {
      newFieldNode.setProperty( DictionaryConst.PROPERTY_KETTLE_TYPE, inputFieldValueMeta != null ? inputFieldValueMeta
          .getTypeDesc() : fieldInString + " unknown type" );
    } else {
      metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_DELETES, fieldNode );
      metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_CREATES, newFieldNode );
    }
    return changeRecord;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( final ReplaceStringMeta stringsReplaceMeta )
    throws MetaverseAnalyzerException {
    renameIndex.clear();
    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>();
    for ( int i = 0; i < stringsReplaceMeta.getFieldInStream().length; i++ ) {
      ComponentDerivationRecord changeRecord = buildChangeRecord( stringsReplaceMeta, i );
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
  public Set<IFieldMapping> getFieldMappings( ReplaceStringMeta meta ) throws MetaverseAnalyzerException {
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

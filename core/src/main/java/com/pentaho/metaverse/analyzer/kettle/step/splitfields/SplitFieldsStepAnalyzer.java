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

package com.pentaho.metaverse.analyzer.kettle.step.splitfields;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.ChangeType;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.api.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.api.model.Operation;
import com.pentaho.metaverse.api.model.kettle.FieldMapping;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.fieldsplitter.FieldSplitterMeta;

import java.util.HashSet;
import java.util.Set;

/**
 * Step analyzer for Field Splitter step
 */
public class SplitFieldsStepAnalyzer extends BaseStepAnalyzer<FieldSplitterMeta> {
  private IComponentDescriptor descriptor;

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, FieldSplitterMeta meta )
    throws MetaverseAnalyzerException {

    IMetaverseNode node = super.analyze( descriptor, meta );

    node.setProperty( DictionaryConst.PROPERTY_DELIMITER, meta.getDelimiter() );
    node.setProperty( DictionaryConst.PROPERTY_ENCLOSURE, meta.getEnclosure() );
    this.descriptor = descriptor;

    getChangeRecords( meta );

    return node;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( FieldSplitterMeta meta )
    throws MetaverseAnalyzerException {

    String originalField = meta.getSplitField();

    // We can't use our own descriptor here, we need to get the descriptor for the origin step
    IMetaverseNode originalFieldNode =
      createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, originalField ) );

    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, originalFieldNode );

    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>();

    for ( int i = 0; i < meta.getFieldName().length; i++ ) {
      String resultingField = meta.getFieldName()[i];

      // Not sure if we need a new node or not, but the builder will take care of it, so just create a node
      // so we can add the "derives" link
      IComponentDescriptor resultingFieldDescriptor = getStepFieldOriginDescriptor( descriptor, resultingField );
      IMetaverseNode resultingFieldNode = createNodeFromDescriptor( resultingFieldDescriptor );

      metaverseBuilder.addNode( resultingFieldNode );

      ComponentDerivationRecord cdr = new ComponentDerivationRecord(
        originalField,
        meta.getFieldName()[i],
        ChangeType.DATA );

      cdr.addOperation( new Operation( Operation.MAPPING_CATEGORY, ChangeType.DATA, meta.getFieldName()[ i ],
        "Token " + i + " of split string" ) );

      // add the derives link and change record
      processFieldChangeRecord( descriptor, originalFieldNode, cdr );

      // if one of the resulting fields is named the same as the original field, be sure to add a created link
      // and a deletes link to the original field
      if ( originalField.equals( resultingField ) ) {
        metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_CREATES, resultingFieldNode );
        metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_DELETES, originalFieldNode );
      }

      changeRecords.add( cdr );
    }

    return changeRecords;
  }

  @Override
  public Set<IFieldMapping> getFieldMappings( FieldSplitterMeta meta ) throws MetaverseAnalyzerException {
    Set<IFieldMapping> fieldMappings = new HashSet<IFieldMapping>();

    String originalField = meta.getSplitField();
    for ( int i = 0; i < meta.getFieldName().length; i++ ) {
      String resultingField = meta.getFieldName()[ i ];
      FieldMapping fieldMapping = new FieldMapping( originalField, resultingField );
      fieldMappings.add( fieldMapping );
    }

    return fieldMappings;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    HashSet<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<Class<? extends BaseStepMeta>>();
    supportedSteps.add( FieldSplitterMeta.class );
    return supportedSteps;
  }

}

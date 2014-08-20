/*!
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
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectMetadataChange;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.HashSet;
import java.util.Set;

/**
 * The SelectValuesStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the
 * fields operated on by Select Values steps.
 */
public class SelectValuesStepAnalyzer extends BaseStepAnalyzer<SelectValuesMeta> {

  /**
   * This value is used by Select Values to indicate "no change" to a particular piece of metadata (precision, e.g.)
   */
  protected static final int NOT_CHANGED = -2;

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IAnalyzer#analyze(IMetaverseComponentDescriptor,java.lang.Object)
   */
  @Override
  public IMetaverseNode analyze( IMetaverseComponentDescriptor descriptor, SelectValuesMeta selectValuesMeta )
    throws MetaverseAnalyzerException {

    // Do common analysis for all steps
    IMetaverseNode node = super.analyze( descriptor, selectValuesMeta );

    IMetaverseNode fieldNode;
    IMetaverseNode newFieldNode;

    // Process the fields/tabs in the same order as the real step does
    if ( !Const.isEmpty( selectValuesMeta.getSelectName() ) ) {
      String[] fieldNames = selectValuesMeta.getSelectName();
      String[] fieldRenames = selectValuesMeta.getSelectRename();
      int[] fieldLength = selectValuesMeta.getSelectLength();
      int[] fieldPrecision = selectValuesMeta.getSelectPrecision();

      for ( int i = 0; i < fieldNames.length; i++ ) {
        String inputFieldName = fieldNames[i];
        String outputFieldName = fieldRenames[i];

        // We can't use our own descriptor here, we need to get the descriptor for the origin step
        fieldNode =
            createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, inputFieldName ) );

        ComponentDerivationRecord changeRecord = new ComponentDerivationRecord( inputFieldName );

        // NOTE: We use equalsIgnoreCase instead of equals because that's how Select Values currently works
        if ( inputFieldName != null && outputFieldName != null && !inputFieldName
            .equalsIgnoreCase( outputFieldName ) ) {
          changeRecord.setEntityName( outputFieldName );
          changeRecord.addOperand( "modified", "name" );
        }

        // Check for changes in field length
        if ( fieldLength != null && fieldLength[i] != NOT_CHANGED ) {
          changeRecord.addOperand( "modified", "length" );
        }

        // Check for changes in field precision
        if ( fieldPrecision != null && fieldPrecision[i] != NOT_CHANGED ) {
          changeRecord.addOperand( "modified", "precision" );
        }

        // There should be at least one operation in order to create a new stream field
        if ( changeRecord.hasDelta() ) {
          // Create a new node for the renamed field
          IMetaverseComponentDescriptor newFieldDescriptor = getChildComponentDescriptor(
              descriptor, changeRecord.getEntityName(), DictionaryConst.NODE_TYPE_TRANS_FIELD );
          newFieldNode = createNodeFromDescriptor( newFieldDescriptor );
          newFieldNode.setProperty( "operations", changeRecord.toString() );
          metaverseBuilder.addNode( newFieldNode );
          metaverseBuilder.addLink( fieldNode, DictionaryConst.LINK_DERIVES, newFieldNode );
          changeRecords.put( changeRecord.getEntityName(), changeRecord );
        }
      }
    }

    // No need to do the Remove tab, the SelectValuesMeta.getFields() will not include removed fields, and the
    // super.analyze() will notice and created a "deleted" relationship for each

    if ( !Const.isEmpty( selectValuesMeta.getMeta() ) ) {
      SelectMetadataChange[] metadataChanges = selectValuesMeta.getMeta();
    }

    return rootNode;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( SelectValuesMeta.class );
      }
    };
  }
}

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

package com.pentaho.metaverse.analyzer.kettle.step.rowsfromresult;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.StepField;
import com.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import com.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.rowsfromresult.RowsFromResultMeta;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by rfellows on 5/13/15.
 */
public class RowsFromResultStepAnalyzer extends StepAnalyzer<RowsFromResultMeta> {

  @Override
  protected StepNodes processInputs( RowsFromResultMeta meta ) {
    StepNodes inputs = new StepNodes();

    // get all input steps
    String[] fieldNames = meta.getFieldname();

    for ( int j = 0; j < fieldNames.length; j++ ) {
      String fieldName = fieldNames[ j ];
      RowMetaInterface rmi = null;
      try {
        rmi = parentTransMeta.getStepFields( parentStepMeta );
        int type = rmi.getValueMeta( j ).getType();
        ValueMetaInterface vmi = rmi.getValueMeta( j );
        //        IMetaverseNode prevFieldNode = createInputFieldNode( StepAnalyzer.NONE, fieldName, type );
        IMetaverseNode prevFieldNode = createInputFieldNode( descriptor.getContext(), vmi, StepAnalyzer.NONE, getInputNodeType() );
        getMetaverseBuilder().addLink( prevFieldNode, DictionaryConst.LINK_INPUTS, rootNode );
        inputs.addNode( StepAnalyzer.NONE, fieldName, prevFieldNode );
      } catch ( KettleStepException e ) {
        // eat it
      }
    }
    return inputs;
  }


  @Override
  protected Set<StepField> getUsedFields( RowsFromResultMeta meta ) {
    return null;
  }

  @Override
  protected void customAnalyze( RowsFromResultMeta meta, IMetaverseNode rootNode )
    throws MetaverseAnalyzerException {
    // nothing custom
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    supportedSteps.add( RowsFromResultMeta.class );
    return supportedSteps;
  }
}

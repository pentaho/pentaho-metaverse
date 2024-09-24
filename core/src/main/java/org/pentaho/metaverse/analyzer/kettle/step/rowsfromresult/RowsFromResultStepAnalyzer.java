/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.analyzer.kettle.step.rowsfromresult;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.recordsfromstream.RecordsFromStreamMeta;
import org.pentaho.di.trans.steps.rowsfromresult.RowsFromResultMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;

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
    supportedSteps.add( RecordsFromStreamMeta.class );
    return supportedSteps;
  }
}

/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.singlethreader;

import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.singlethreader.SingleThreaderMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;

import java.util.HashSet;
import java.util.Set;

public class SingleThreaderStepAnalyzer extends StepAnalyzer<SingleThreaderMeta> {
  @Override protected Set<StepField> getUsedFields( SingleThreaderMeta meta ) {
    return null;
  }

  @Override protected void customAnalyze( SingleThreaderMeta meta, IMetaverseNode rootNode ) throws
    MetaverseAnalyzerException {

    final String injectStepName = meta.getInjectStep();

    // Add the injector/retrieval step info to the root node properties as well as the batch size
    rootNode.setProperty( "injectorStep", parentTransMeta.environmentSubstitute( injectStepName ) );
    rootNode.setProperty( "retrieveStep", parentTransMeta.environmentSubstitute( meta.getRetrieveStep() ) );
    rootNode.setProperty( "batchSize", parentTransMeta.environmentSubstitute( meta.getBatchSize() ) );
    rootNode.setProperty( "batchTime", parentTransMeta.environmentSubstitute( meta.getBatchTime() ) );

    // Get the subtrans
    final TransMeta subTransMeta = KettleAnalyzerUtil.getSubTransMeta( meta );

    // Create a node for the subtrans
    final IMetaverseNode subTransNode = getNode( subTransMeta.getName(), DictionaryConst.NODE_TYPE_TRANS,
      descriptor.getNamespace(), null, null );

    // Set SubTrans file path and ID on subtrans node
    subTransNode.setProperty( DictionaryConst.PROPERTY_PATH,
      KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ) );
    subTransNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

    // Add the new subtrans node to the output
    // TODO: Our static-y analyzers don't give us access to the subtrans data , this will need to be revisited
    // when analyzers become more dynamic
    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_EXECUTES, subTransNode );
  }

  @Override public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( SingleThreaderMeta.class );
      }
    };
  }
}

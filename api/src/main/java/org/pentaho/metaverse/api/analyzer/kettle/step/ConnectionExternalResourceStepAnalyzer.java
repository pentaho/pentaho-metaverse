/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Set;

/**
 * Created by rfellows on 5/26/15.
 */
public abstract class ConnectionExternalResourceStepAnalyzer<T extends BaseStepMeta>
  extends ExternalResourceStepAnalyzer<T> {

  protected IMetaverseNode connectionNode = null;
  protected IMetaverseNode tableNode = null;

  @Override
  protected void customAnalyze( T meta, IMetaverseNode node ) throws MetaverseAnalyzerException {
    super.customAnalyze( meta, node );

    IMetaverseNode connectionNode = getConnectionNode();
    // add a node for the connection itself
    getMetaverseBuilder().addNode( connectionNode );
    // link the connection to the step
    getMetaverseBuilder().addLink( connectionNode, DictionaryConst.LINK_DEPENDENCYOF, node );

  }

  @Override
  public IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException {
    tableNode = createTableNode( resource );
    StepNodes stepNodes = isInput() ? getInputs() : getOutputs();
    linkResourceToFields( stepNodes );
    return tableNode;
  }

  public IMetaverseNode getConnectionNode() throws MetaverseAnalyzerException {
    if ( connectionNode == null ) {
      connectionNode = (IMetaverseNode) getConnectionAnalyzer().analyze( getDescriptor(), baseStepMeta );
    }
    return connectionNode;
  }

  protected abstract IMetaverseNode createTableNode( IExternalResourceInfo resource ) throws MetaverseAnalyzerException;

  public IMetaverseNode getTableNode() {
    return tableNode;
  }

  public void linkResourceToFields( StepNodes stepNodes ) {
    // link in the resource nodes as "contains" to the table node
    Set<String> fieldNames = stepNodes.getFieldNames( ExternalResourceStepAnalyzer.RESOURCE );
    if ( fieldNames != null ) {
      for ( String fieldName : fieldNames ) {
        IMetaverseNode resNode = stepNodes.findNode( ExternalResourceStepAnalyzer.RESOURCE, fieldName );
        if ( resNode != null ) {
          // set the column's namespace to the logical id of the table
          resNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, getTableNode().getLogicalId() );
          // update the db column node to force the logical id to re-generate with the updated namespace
          getMetaverseBuilder().updateNode( resNode );
          getMetaverseBuilder().addLink( getTableNode(), DictionaryConst.LINK_CONTAINS, resNode );
        }
      }
    }
  }

}

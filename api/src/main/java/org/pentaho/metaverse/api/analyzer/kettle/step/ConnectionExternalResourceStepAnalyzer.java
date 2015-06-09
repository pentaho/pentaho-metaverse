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

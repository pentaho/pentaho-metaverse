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

package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.DictionaryHelper;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

/**
 * The TableOutputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and
 * other metaverse entities (such as physical fields and tables
 * 
 */
public class TableOutputStepAnalyzer extends AbstractAnalyzer<TableOutputMeta> {

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IAnalyzer#analyze(java.lang.Object)
   */
  @Override
  public IMetaverseNode analyze( TableOutputMeta object ) throws MetaverseAnalyzerException {


    // Add yourself
    IMetaverseNode node = new KettleStepAnalyzer().analyze( object.getParentStepMeta() );

    TransMeta transMeta = object.getParentStepMeta().getParentTransMeta();

    RowMetaInterface rmi;
    try {
      rmi = transMeta.getPrevStepFields( object.getParentStepMeta() );
    } catch ( KettleStepException e ) {
      throw new MetaverseAnalyzerException( e );
    }

    String tableName = object.getTableName();
    String[] fieldNames = {};

    if ( rmi != null ) {
      fieldNames = rmi.getFieldNames();
    }

    String type = DictionaryConst.NODE_TYPE_DATA_TABLE;

    IMetaverseNode tableNode = metaverseObjectFactory.createNodeObject(
        DictionaryHelper.getId( type, object.getDatabaseMeta().getName(), tableName ) );

    tableNode.setType( type );
    tableNode.setName( tableName );

    metaverseBuilder.addNode( tableNode );

    // TODO originally, this link was from the trans to the table ... do we need that link?
    // TODO if so, we may need access to the parent chain of nodes to skip levels
    metaverseBuilder.addLink( node, DictionaryConst.LINK_WRITESTO, tableNode );

    type = DictionaryConst.NODE_TYPE_TRANS_FIELD;
    IMetaverseNode fieldNode;

    for ( String fieldName : fieldNames ) {
      fieldNode = metaverseObjectFactory.createNodeObject(
          DictionaryHelper.getId( type, object.getDatabaseMeta().getName(), tableName, fieldName ) );

      fieldNode.setName( fieldName );
      fieldNode.setType( type );

      metaverseBuilder.addNode( fieldNode );
      metaverseBuilder.addLink( tableNode, DictionaryConst.LINK_CONTAINS, fieldNode );
    }

    return node;
  }

}

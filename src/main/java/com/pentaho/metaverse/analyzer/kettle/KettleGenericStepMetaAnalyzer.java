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
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.List;

/**
 * KettleGenericStepMetaAnalyzer provides a default implementation for analyzing PDI steps to gather metadata for the metaverse.
 */
public class KettleGenericStepMetaAnalyzer extends AbstractAnalyzer<StepMeta> {

  /**
   * Analyzes a step to gather metadata (such as input/output fields, used database connections, etc.)
   *
   * @see org.pentaho.platform.api.metaverse.IAnalyzer#analyze(java.lang.Object)
   */
  @Override
  public IMetaverseNode analyze( StepMeta stepMeta ) throws MetaverseAnalyzerException {

    if ( stepMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.StepMeta.IsNull" ) );
    }

    StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();

    if ( stepMetaInterface == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.StepMetaInterface.IsNull" ) );
    }

    if ( metaverseBuilder == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseBuilder.IsNull" ) );
    }

    if ( metaverseObjectFactory == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseObjectFactory.IsNull" ) );
    }

    // Add yourself
    IMetaverseNode node = metaverseObjectFactory.createNodeObject(
        DictionaryHelper.getId( stepMeta.getClass(), stepMeta.getName() ) );

    node.setName( stepMeta.getName() );
    node.setType( DictionaryConst.NODE_TYPE_TRANS_STEP );

    metaverseBuilder.addNode( node );

    DatabaseMeta[] dbs = stepMetaInterface.getUsedDatabaseConnections();

    if ( dbs != null ) {

      DatabaseConnectionAnalyzer dbAnalyzer = getDatabaseConnectionAnalyzer();
      for ( DatabaseMeta db : dbs ) {
        IMetaverseNode dbNode = dbAnalyzer.analyze( db );
        metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, dbNode );
      }

    }

    // TODO Investigate interfaces to see what default input &/or
    // TODO output fields are available ... process those here
    try {
      TransMeta parentTrans = stepMeta.getParentTransMeta();
      if ( parentTrans != null ) {
        RowMetaInterface incomingRow = parentTrans.getPrevStepFields( stepMeta );
        RowMetaInterface outgoingRow = parentTrans.getStepFields( stepMeta );

        if ( outgoingRow != null ) {

          // Find fields that were created by this step
          List<ValueMetaInterface> outRowValueMetas = outgoingRow.getValueMetaList();
          if ( outRowValueMetas != null ) {
            for ( ValueMetaInterface outRowMeta : outRowValueMetas ) {
              if ( incomingRow == null || incomingRow.searchValueMeta( outRowMeta.getName() ) == null ) {
                // This field didn't come into the step, so assume it has been created here
                IMetaverseNode newFieldNode = metaverseObjectFactory.createNodeObject(
                    DictionaryHelper.getId( DictionaryConst.NODE_TYPE_TRANS_FIELD, outRowMeta.getName() ) );
                newFieldNode.setName( outRowMeta.getName() );
                newFieldNode.setType( DictionaryConst.NODE_TYPE_TRANS_FIELD );
                newFieldNode.setProperty( "kettleType", outRowMeta.getTypeDesc() );

                metaverseBuilder.addNode( newFieldNode );

                // Add link to show that this step created the field
                metaverseBuilder.addLink( node, DictionaryConst.LINK_CREATES, newFieldNode );
              }
            }
          }
        }
      }
    } catch ( KettleException ke ) {
      throw new MetaverseAnalyzerException( ke );
    }
    return node;
  }

  protected DatabaseConnectionAnalyzer getDatabaseConnectionAnalyzer() {

    DatabaseConnectionAnalyzer analyzer = new DatabaseConnectionAnalyzer();
    analyzer.setMetaverseObjectFactory( metaverseObjectFactory );
    analyzer.setMetaverseBuilder( metaverseBuilder );

    return analyzer;

  }

}

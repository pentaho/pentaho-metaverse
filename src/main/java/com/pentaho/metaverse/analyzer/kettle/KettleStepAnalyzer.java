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
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

/**
 * KettleStepAnalyzer provides a default implementation for analyzing PDI steps to gather metadata for the metaverse.
 */
public class KettleStepAnalyzer extends AbstractAnalyzer<StepMeta> {


  /**
   * Analyzes a step to gather metadata (such as input/output fields, used database connections, etc.)
   * 
   * @see org.pentaho.platform.api.metaverse.IAnalyzer#analyze(java.lang.Object)
   */
  @Override
  public IMetaverseNode analyze( StepMeta stepMeta ) throws MetaverseAnalyzerException {

    if ( stepMeta == null ) {
      throw new MetaverseAnalyzerException( "StepMeta is null!" );
    }

    StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();

    if ( stepMetaInterface == null ) {
      throw new MetaverseAnalyzerException( "StepMetaInterface is null!" );
    }

    if ( metaverseObjectFactory == null ) {
      throw new MetaverseAnalyzerException( "MetaverseObjectFactory is null!" );
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
        metaverseBuilder.addLink( dbNode, DictionaryConst.LINK_USEDBY, node );
      }

    }

    return node;
  }

  private DatabaseConnectionAnalyzer getDatabaseConnectionAnalyzer() {

    DatabaseConnectionAnalyzer analyzer = new DatabaseConnectionAnalyzer();
    analyzer.setMetaverseObjectFactory( metaverseObjectFactory );
    analyzer.setMetaverseBuilder( metaverseBuilder );
    return analyzer;

  }

}

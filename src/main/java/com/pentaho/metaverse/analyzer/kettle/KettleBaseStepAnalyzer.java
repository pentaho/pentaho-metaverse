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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.List;

/**
 * KettleBaseStepAnalyzer provides a default implementation (and generic helper methods) for analyzing PDI steps
 * to gather metadata for the metaverse.
 */
public abstract class KettleBaseStepAnalyzer<T extends BaseStepMeta>
    extends BaseKettleMetaverseComponent implements IStepAnalyzer<T> {

  /**
   * The stream fields coming into the step
   */
  protected RowMetaInterface prevFields = null;

  /**
   * The stream fields coming out of the step
   */
  protected RowMetaInterface stepFields = null;

  /**
   * A reference to the step under analysis
   */
  protected BaseStepMeta baseStepMeta = null;

  /**
   * The step's parent StepMeta object (to get the parent TransMeta, in/out fields, etc.)
   */
  protected StepMeta parentStepMeta = null;

  /**
   * A reference to the transformation that contains the step under analysis
   */
  protected TransMeta parentTransMeta = null;

  /**
   * A reference to the root node created by the analyzer (usually corresponds to the step under analysis)
   */
  protected IMetaverseNode rootNode = null;

  /**
   * Analyzes a step to gather metadata (such as input/output fields, used database connections, etc.)
   *
   * @see org.pentaho.platform.api.metaverse.IAnalyzer#analyze(Object)
   */
  @Override
  public IMetaverseNode analyze( T object ) throws MetaverseAnalyzerException {

    baseStepMeta = object;
    if ( baseStepMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.StepMetaInterface.IsNull" ) );
    }

    parentStepMeta = baseStepMeta.getParentStepMeta();
    if ( parentStepMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.StepMeta.IsNull" ) );
    }

    parentTransMeta = parentStepMeta.getParentTransMeta();

    if ( metaverseBuilder == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseBuilder.IsNull" ) );
    }

    if ( metaverseObjectFactory == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseObjectFactory.IsNull" ) );
    }

    // Add yourself
    rootNode = addSelfNode();

    // Add database connection nodes
    addDatabaseConnectionNodes();

    // Interrogate API to see what default field information is available
    loadInputAndOutputStreamFields();
    addCreatedFieldNodes();
    return rootNode;
  }

  /**
   * Adds to the metaverse the step under analysis
   *
   * @return the metaverse node corresponding to the step under analysis
   * @throws MetaverseAnalyzerException
   */
  protected IMetaverseNode addSelfNode() throws MetaverseAnalyzerException {
    try {
      IMetaverseNode node = metaverseObjectFactory.createNodeObject(
          DictionaryHelper.getId( BaseStepMeta.class, parentStepMeta.getName() ),
          parentStepMeta.getName(),
          DictionaryConst.NODE_TYPE_TRANS_STEP );

      metaverseBuilder.addNode( node );
      return node;
    } catch ( Throwable t ) {
      // Wrap exceptions as a MAE
      throw new MetaverseAnalyzerException( t );
    }
  }

  /**
   * Adds any used database connections to the metaverse using the appropriate analyzer
   *
   * @throws MetaverseAnalyzerException
   */
  protected void addDatabaseConnectionNodes() throws MetaverseAnalyzerException {
    if ( baseStepMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.StepMetaInterface.IsNull" ) );
    }

    // Analyze the database connections
    DatabaseMeta[] dbs = baseStepMeta.getUsedDatabaseConnections();
    DatabaseConnectionAnalyzer dbAnalyzer = getDatabaseConnectionAnalyzer();
    if ( dbs != null && dbAnalyzer != null ) {
      for ( DatabaseMeta db : dbs ) {
        IMetaverseNode dbNode = dbAnalyzer.analyze( db );
        metaverseBuilder.addLink( dbNode, DictionaryConst.LINK_DEPENDENCYOF, rootNode );
      }
    }
  }

  /**
   * Adds to the metaverse any fields created by this step
   */
  protected void addCreatedFieldNodes() {
    try {
      if ( stepFields != null ) {
        // Find fields that were created by this step
        List<ValueMetaInterface> outRowValueMetas = stepFields.getValueMetaList();
        if ( outRowValueMetas != null ) {
          for ( ValueMetaInterface outRowMeta : outRowValueMetas ) {
            if ( prevFields != null && prevFields.searchValueMeta( outRowMeta.getName() ) == null ) {
              // This field didn't come into the step, so assume it has been created here
              IMetaverseNode newFieldNode = metaverseObjectFactory.createNodeObject(
                  DictionaryHelper.getId( DictionaryConst.NODE_TYPE_TRANS_FIELD,
                      parentStepMeta.getName(),
                      outRowMeta.getName() ) );
              newFieldNode.setName( outRowMeta.getName() );
              newFieldNode.setType( DictionaryConst.NODE_TYPE_TRANS_FIELD );
              newFieldNode.setProperty( "kettleType", outRowMeta.getTypeDesc() );

              metaverseBuilder.addNode( newFieldNode );

              // Add link to show that this step created the field
              metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_CREATES, newFieldNode );
            }
            // no else clause: if we can't determine the fields, we can't do anything else
          }
        }
      }
    } catch ( Throwable t ) {
      // TODO Don't throw an exception here, just log the error and move on
      t.printStackTrace( System.err );
    }
  }

  /**
   * Loads the in/out fields for this step into member variables for use by the analyzer
   */
  protected void loadInputAndOutputStreamFields() {
    if ( parentTransMeta != null ) {
      try {
        prevFields = parentTransMeta.getPrevStepFields( parentStepMeta );
      } catch ( Throwable t ) {
        prevFields = null;
      }
      try {
        stepFields = parentTransMeta.getStepFields( parentStepMeta );
      } catch ( Throwable t ) {
        stepFields = null;
      }
    }
  }

  /**
   * Returns an object capable of analyzing database connections (DatabaseMetas)
   *
   * @return a database connection Analyzer
   */
  protected DatabaseConnectionAnalyzer getDatabaseConnectionAnalyzer() {

    DatabaseConnectionAnalyzer analyzer = new DatabaseConnectionAnalyzer();
    analyzer.setMetaverseBuilder( metaverseBuilder );
    return analyzer;
  }

}

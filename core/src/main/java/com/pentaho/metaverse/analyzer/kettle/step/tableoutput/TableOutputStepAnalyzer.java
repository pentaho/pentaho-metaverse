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

package com.pentaho.metaverse.analyzer.kettle.step.tableoutput;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.ConnectionExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.model.BaseDatabaseResourceInfo;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.apache.commons.lang.ArrayUtils;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The TableOutputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and
 * other metaverse entities (such as physical fields and tables)
 */
public class TableOutputStepAnalyzer extends ConnectionExternalResourceStepAnalyzer<TableOutputMeta> {

  public static final String TRUNCATE_TABLE = "truncateTable";

  @Override
  protected void customAnalyze( TableOutputMeta meta, IMetaverseNode node )
    throws MetaverseAnalyzerException {
    super.customAnalyze( meta, node );
    boolean truncate = meta.truncateTable();
    node.setProperty( TRUNCATE_TABLE, Boolean.valueOf( truncate ) );
  }

  @Override protected IMetaverseNode createTableNode( IExternalResourceInfo resource )
    throws MetaverseAnalyzerException {
    BaseDatabaseResourceInfo resourceInfo = (BaseDatabaseResourceInfo) resource;

    Object obj = resourceInfo.getAttributes().get( DictionaryConst.PROPERTY_TABLE );
    String tableName = obj == null ? null : obj.toString();
    obj = resourceInfo.getAttributes().get( DictionaryConst.PROPERTY_SCHEMA );
    String schema = obj == null ? null : obj.toString();

    // create a node for the table
    MetaverseComponentDescriptor componentDescriptor = new MetaverseComponentDescriptor(
      tableName,
      DictionaryConst.NODE_TYPE_DATA_TABLE,
      getConnectionNode(),
      getDescriptor().getContext() );

    // set the namespace to be the id of the connection node.
    IMetaverseNode tableNode = createNodeFromDescriptor( componentDescriptor );
    tableNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, componentDescriptor.getNamespace().getNamespaceId() );
    tableNode.setProperty( DictionaryConst.PROPERTY_TABLE, tableName );
    tableNode.setProperty( DictionaryConst.PROPERTY_SCHEMA, schema );
    tableNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DB_TABLE );
    return tableNode;
  }

  @Override
  protected Map<String, RowMetaInterface> getOutputRowMetaInterfaces( TableOutputMeta meta ) {
    String[] nextStepNames = parentTransMeta.getNextStepNames( parentStepMeta );
    Map<String, RowMetaInterface> outputRows = new HashMap<>();
    RowMetaInterface outputFields = getOutputFields( meta );

    if ( outputFields != null && ArrayUtils.isEmpty( nextStepNames ) ) {
      nextStepNames = new String[] { NONE };
    }
    for ( String stepName : nextStepNames ) {
      outputRows.put( stepName, outputFields );
    }

    RowMetaInterface tableFields = new RowMeta();

    Set<String> outFields = getOutputResourceFields( meta );
    for ( String outField : outFields ) {
      ValueMetaInterface vmi = new ValueMeta( outField );
      tableFields.addValueMeta( vmi );
    }
    outputRows.put( RESOURCE, tableFields );

    return outputRows;
  }

  @Override
  public Set<String> getOutputResourceFields( TableOutputMeta meta ) {
    Set<String> fields = new LinkedHashSet<>( Arrays.asList( meta.getFieldDatabase() ) );
    return fields;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( TableOutputMeta meta ) throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changes = new HashSet<>();
    String[] tableFields = meta.getFieldDatabase();
    String[] streamFields = meta.getFieldStream();
    if ( getInputs() != null ) {
      Set<String> stepNames = getInputs().getStepNames();
      for ( int i = 0; i < tableFields.length; i++ ) {
        String tableField = tableFields[ i ];
        String streamField = streamFields[ i ];
        for ( String stepName : stepNames ) {
          if ( !RESOURCE.equals( stepName ) ) {
            StepField inputField = new StepField( stepName, streamField );
            StepField outField = new StepField( RESOURCE, tableField );
            ComponentDerivationRecord change = new ComponentDerivationRecord( inputField, outField );
            changes.add( change );
          }
        }
      }
    }
    return changes;
  }

  @Override
  public IMetaverseNode getConnectionNode() throws MetaverseAnalyzerException {
    if ( connectionNode == null ) {
      connectionNode = (IMetaverseNode) getConnectionAnalyzer().analyze(
        getDescriptor(), baseStepMeta.getDatabaseMeta() );
    }
    return connectionNode;
  }

  @Override
  protected Set<StepField> getUsedFields( TableOutputMeta meta ) {
    return null;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( TableOutputMeta.class );
      }
    };
  }

  @Override
  public String getResourceInputNodeType() {
    return null;
  }

  @Override
  public String getResourceOutputNodeType() {
    return DictionaryConst.NODE_TYPE_DATA_COLUMN;
  }

  @Override
  public boolean isOutput() {
    return true;
  }

  @Override
  public boolean isInput() {
    return false;
  }

  ///////////// for unit testing
  protected void setBaseStepMeta( TableOutputMeta meta ) {
    baseStepMeta = meta;
  }
  protected void setRootNode( IMetaverseNode node ) {
    rootNode = node;
  }
  protected void setParentTransMeta( TransMeta tm ) {
    parentTransMeta = tm;
  }
  protected void setParentStepMeta( StepMeta sm ) {
    parentStepMeta = sm;
  }
}

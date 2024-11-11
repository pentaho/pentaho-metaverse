/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.step.tableoutput;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.ConnectionExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.model.BaseDatabaseResourceInfo;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

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
    String tableName = obj == null ? null : parentTransMeta.environmentSubstitute( obj.toString() );
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

  /**
   * Get the resource fields actually written to the table. Normally return fields specified in table settings.
   * If no fields specified and "specify database fields" option is unchecked, return regular output fields.
   *
   * @param meta Table Output component metadata
   * @return set of resource field names
   */
  @Override
  public Set<String> getOutputResourceFields( TableOutputMeta meta ) {
    String[] fieldArray = meta.getFieldDatabase();
    if ( ArrayUtils.isEmpty( fieldArray ) || !meta.specifyFields() ) {
      fieldArray = getOutputFields( meta ).getFieldNames();
    }
    Set<String> fields = new LinkedHashSet<>( Arrays.asList( fieldArray ) );
    return fields;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( TableOutputMeta meta ) throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changes = new HashSet<>();
    String[] tableFields = meta.specifyFields() ? meta.getFieldDatabase() : new String[]{};
    String[] streamFields = meta.specifyFields() ? meta.getFieldStream() : new String[]{};
    if ( getInputs() != null ) {
      Set<String> stepNames = getInputs().getStepNames();
      for ( int i = 0; i < tableFields.length; i++ ) {
        String tableField = tableFields[ i ];
        String streamField = streamFields[ i ];
        for ( String stepName : stepNames ) {
          if ( !RESOURCE.equals( stepName ) && !StringUtils.equals( tableField,  streamField ) ) {
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
    connectionNode = (IMetaverseNode) getConnectionAnalyzer().analyze(
        getDescriptor(), baseStepMeta.getDatabaseMeta() );
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

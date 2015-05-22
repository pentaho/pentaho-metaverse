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
 */

package com.pentaho.metaverse.analyzer.kettle.step.mongodbinput;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IAnalysisContext;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.MetaverseException;
import com.pentaho.metaverse.api.StepField;
import com.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.impl.model.MongoDbResourceInfo;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mongodbinput.MongoDbInputMeta;
import org.pentaho.mongo.wrapper.field.MongoField;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Analyzes MongoDbInput Steps for lineage related information
 */
public class MongoDbInputStepAnalyzer extends ExternalResourceStepAnalyzer<MongoDbInputMeta> {

  public static final String COLLECTION = "collection";

  // Query property names
  public static final String AGG_PIPELINE = "isAggPipeline";
  public static final String FIELDS_EXPRESSION = "fieldsExpression";

  // Tag Set / Read Preference property names
  public static final String READ_PREF = "readPreference";
  public static final String TAG_SETS = "tagSets";

  // Field property names
  public static final String OUTPUT_JSON = "outputJson";
  public static final String JSON_PATH = "jsonPath";
  public static final String MINMAX_RANGE = "minMaxArrayRange";
  public static final String OCCUR_RATIO = "occurRatio";
  public static final String INDEXED_VALS = "indexedValues";
  public static final String DISPARATE_TYPES = "disparateTypes";

  @Override protected void customAnalyze( MongoDbInputMeta meta, IMetaverseNode node )
    throws MetaverseAnalyzerException {
    super.customAnalyze( meta, node );

    node.setProperty( OUTPUT_JSON, meta.getOutputJson() );
    node.setProperty( COLLECTION, meta.getCollection() );
    // If the output is the full JSON, we don't have to do any additional analysis
    if ( !meta.getOutputJson() ) {
      // add properties to the node - the query (and its characteristics) in particular
      node.setProperty( DictionaryConst.PROPERTY_QUERY, meta.getJsonQuery() );
      node.setProperty( AGG_PIPELINE, meta.getQueryIsPipeline() );
      node.setProperty( DictionaryConst.PROPERTY_EXECUTE_EACH_ROW, meta.getExecuteForEachIncomingRow() );
      node.setProperty( FIELDS_EXPRESSION, meta.getFieldsName() );

      // Add tag set and read preference information to the step node
      node.setProperty( READ_PREF, meta.getReadPreference() );
      List<String> tagSets = meta.getReadPrefTagSets();
      if ( tagSets != null ) {
        // The string representation of the list is good enough for us. Each entry should already be in JSON format
        node.setProperty( TAG_SETS, tagSets.toString() );
      }
    }
  }

  @Override
  protected IMetaverseNode createOutputFieldNode( IAnalysisContext context, ValueMetaInterface fieldMeta,
                                                            String targetStepName, String nodeType ) {

    IMetaverseNode mongoFieldNode = super.createOutputFieldNode( context, fieldMeta, targetStepName, nodeType );
    List<MongoField> mongoFields = baseStepMeta.getMongoFields();
    if ( mongoFields != null ) {
      for ( MongoField mongoField : mongoFields ) {
        if ( fieldMeta.getName().equals( mongoField.getName() ) ) {
          mongoFieldNode.setProperty( JSON_PATH, mongoField.m_fieldPath );
          mongoFieldNode.setProperty( MINMAX_RANGE, mongoField.m_arrayIndexInfo );
          mongoFieldNode.setProperty( OCCUR_RATIO, mongoField.m_occurenceFraction );
          mongoFieldNode.setProperty( INDEXED_VALS, mongoField.m_indexedVals );
          mongoFieldNode.setProperty( DISPARATE_TYPES, mongoField.m_disparateTypes );
          break;
        }
      }
    }
    return mongoFieldNode;
  }

  @Override protected Set<StepField> getUsedFields( MongoDbInputMeta meta ) {
    return null;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<Class<? extends BaseStepMeta>>();
    supportedSteps.add( MongoDbInputMeta.class );
    return supportedSteps;
  }

  @Override public IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException {
    MetaverseComponentDescriptor componentDescriptor = null;
    MongoDbResourceInfo resourceInfo = (MongoDbResourceInfo) resource;

    IMetaverseNode connectionNode = (IMetaverseNode) getConnectionAnalyzer().analyze( descriptor, baseStepMeta );

    // add a node for the connection itself
    getMetaverseBuilder().addNode( connectionNode );
    // link the connection to the step
    getMetaverseBuilder().addLink( connectionNode, DictionaryConst.LINK_DEPENDENCYOF, rootNode );

    // create a node for the collection
    componentDescriptor = new MetaverseComponentDescriptor(
      resourceInfo.getCollection(),
      DictionaryConst.NODE_TYPE_MONGODB_COLLECTION,
      connectionNode,
      descriptor.getContext() );

    // set the namespace to be the id of the connection node.
    IMetaverseNode node = createNodeFromDescriptor( componentDescriptor );
    node.setProperty( DictionaryConst.PROPERTY_NAMESPACE, componentDescriptor.getNamespace().getNamespaceId() );
    node.setProperty( COLLECTION, resourceInfo.getCollection() );
    node.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DEFAULT );
    return node;
  }

  @Override public String getResourceInputNodeType() {
    return DictionaryConst.NODE_TYPE_DATA_COLUMN;
  }

  @Override public String getResourceOutputNodeType() {
    return null;
  }

  @Override public boolean isOutput() {
    return false;
  }

  @Override public boolean isInput() {
    return true;
  }

  ///////////// for unit testing
  protected void setBaseStepMeta( MongoDbInputMeta meta ) {
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

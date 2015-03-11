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
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.impl.Namespace;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.mongodbinput.MongoDbInputMeta;
import org.pentaho.mongo.wrapper.field.MongoField;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Analyzes MongoDbInput Steps for lineage related information
 */
public class MongoDbInputStepAnalyzer extends BaseStepAnalyzer<MongoDbInputMeta> {

  public static final String COLLECTION = "collection";

  // Query property names
  public static final String AGG_PIPELINE = "isAggPipeline";
  public static final String FIELDS_EXPRESSION = "fieldsExpression";

  // Field property names
  public static final String OUTPUT_JSON = "outputJson";
  public static final String JSON_PATH = "jsonPath";
  public static final String MINMAX_RANGE = "minMaxArrayRange";
  public static final String OCCUR_RATIO = "occurRatio";
  public static final String INDEXED_VALS = "indexedValues";
  public static final String DISPARATE_TYPES = "disparateTypes";

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, MongoDbInputMeta meta )
    throws MetaverseAnalyzerException {

    // Let base analyzer handle connections, created fields, etc.
    IMetaverseNode node = super.analyze( descriptor, meta );

    // Create collection node
    String collectionName = meta.getCollection();
    node.setProperty( COLLECTION, collectionName );
    IComponentDescriptor mongoCollectionDescriptor = new MetaverseComponentDescriptor(
      collectionName,
      DictionaryConst.NODE_TYPE_MONGODB_COLLECTION,
      new Namespace( collectionName ),
      descriptor.getContext() );
    mongoCollectionDescriptor.setType( DictionaryConst.NODE_TYPE_MONGODB_COLLECTION );
    IMetaverseNode mongoCollectionNode = createNodeFromDescriptor( mongoCollectionDescriptor );

    metaverseBuilder.addNode( mongoCollectionNode );
    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, mongoCollectionNode );

    node.setProperty( OUTPUT_JSON, meta.getOutputJson() );
    // If the output is the full JSON, we don't have to do any additional analysis
    if ( !meta.getOutputJson() ) {
      // add properties to the node - the query (and its characteristics) in particular
      node.setProperty( DictionaryConst.PROPERTY_QUERY, meta.getJsonQuery() );
      node.setProperty( AGG_PIPELINE, meta.getQueryIsPipeline() );
      node.setProperty( DictionaryConst.PROPERTY_EXECUTE_EACH_ROW, meta.getExecuteForEachIncomingRow() );
      node.setProperty( FIELDS_EXPRESSION, meta.getFieldsName() );

      // Process the fields (if specified)
      List<MongoField> mongoFields = meta.getMongoFields();
      if ( mongoFields != null ) {
        for ( MongoField mongoField : mongoFields ) {
          // Create physical field nodes
          IComponentDescriptor mongoFieldNodeDescriptor = new MetaverseComponentDescriptor(
            mongoField.m_fieldName,
            DictionaryConst.NODE_TYPE_DATA_COLUMN,
            new Namespace( collectionName ),
            descriptor.getContext() );
          IMetaverseNode mongoFieldNode = createNodeFromDescriptor( mongoFieldNodeDescriptor );
          mongoFieldNode.setProperty( JSON_PATH, mongoField.m_fieldPath );
          mongoFieldNode.setProperty( MINMAX_RANGE, mongoField.m_arrayIndexInfo );
          mongoFieldNode.setProperty( OCCUR_RATIO, mongoField.m_occurenceFraction );
          mongoFieldNode.setProperty( INDEXED_VALS, mongoField.m_indexedVals );
          mongoFieldNode.setProperty( DISPARATE_TYPES, mongoField.m_disparateTypes );

          metaverseBuilder.addNode( mongoFieldNode );
          metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, mongoFieldNode );
          metaverseBuilder.addLink( mongoCollectionNode, DictionaryConst.LINK_CONTAINS, mongoFieldNode );

          // Get the stream field output from this step. It will have been created when we called super.analyze()
          IComponentDescriptor transFieldDescriptor =
            getStepFieldOriginDescriptor( descriptor, mongoFieldNode.getName() );
          IMetaverseNode outNode = createNodeFromDescriptor( transFieldDescriptor );
          metaverseBuilder.addLink( mongoFieldNode, DictionaryConst.LINK_POPULATES, outNode );
        }
      }

    }
    return node;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<Class<? extends BaseStepMeta>>();
    supportedSteps.add( MongoDbInputMeta.class );
    return supportedSteps;
  }
}

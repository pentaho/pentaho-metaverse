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

package com.pentaho.metaverse.analyzer.kettle.step.rowstoresult;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.apache.commons.collections.CollectionUtils;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.rowstoresult.RowsToResultMeta;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by rfellows on 4/3/15.
 */
public class RowsToResultStepAnalyzer extends BaseStepAnalyzer<RowsToResultMeta> {

  public static final String RESULT_ROW_FIELD = "resultRowField";

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, RowsToResultMeta meta )
    throws MetaverseAnalyzerException {

    IMetaverseNode node = super.analyze( descriptor, meta );

    // add the stream fields that this creates on the result and link it to the incoming field.
    RowMetaInterface rowMetaInterface = getOutputFields( meta );
    if ( rowMetaInterface != null && !CollectionUtils.isEmpty( rowMetaInterface.getValueMetaList() ) ) {
      for ( ValueMetaInterface vmi : rowMetaInterface.getValueMetaList() ) {

        IComponentDescriptor desc = new MetaverseComponentDescriptor(
          vmi.getName(),
          DictionaryConst.NODE_TYPE_TRANS_FIELD,
          node,
          descriptor.getContext() );

        IMetaverseNode createdNode = createNodeFromDescriptor( desc );
        node.setProperty( RESULT_ROW_FIELD, true );
        createdNode.setProperty( DictionaryConst.PROPERTY_KETTLE_TYPE, vmi.getTypeDesc() );
        metaverseBuilder.addNode( createdNode );

        IComponentDescriptor transFieldDescriptor = getStepFieldOriginDescriptor( descriptor, vmi.getName() );
        IMetaverseNode transFieldNode = createNodeFromDescriptor( transFieldDescriptor );

        metaverseBuilder.addLink( node, DictionaryConst.LINK_CREATES, createdNode );
        metaverseBuilder.addLink( transFieldNode, DictionaryConst.LINK_DERIVES, createdNode );
        metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, transFieldNode );
      }
    } else {
      throw new MetaverseAnalyzerException( "No output fields detected for RowsToResultMeta - " + meta.getName() );
    }
    return node;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    supportedSteps.add( RowsToResultMeta.class );
    return supportedSteps;
  }
}

/*
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

package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.Namespace;
import com.pentaho.metaverse.api.analyzer.kettle.step.BaseStepAnalyzer;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The TableInputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and
 * other metaverse entities
 */
public class TableInputStepAnalyzer extends BaseStepAnalyzer<TableInputMeta> {
  private Logger log = LoggerFactory.getLogger( TableInputStepAnalyzer.class );

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, TableInputMeta tableFileInputMeta )
      throws MetaverseAnalyzerException {

    // do the common analysis for all step
    super.analyze( descriptor, tableFileInputMeta );

    // NOTE: We are assuming for this POC that the column name matches the stream field name, meaning
    // the Table Input SQL script doesn't use aliases, aggregates, etc.
    if ( stepFields != null ) {
      List<ValueMetaInterface> stepFieldValueMetas = stepFields.getValueMetaList();
      for ( ValueMetaInterface fieldMeta : stepFieldValueMetas ) {
        String fieldName = fieldMeta.getName();
        IComponentDescriptor dbColumnDescriptor = new MetaverseComponentDescriptor(
            fieldName,
            DictionaryConst.NODE_TYPE_DATA_COLUMN,
            new Namespace( fieldName ),
            descriptor.getContext() );
        IMetaverseNode dbColumnNode = createNodeFromDescriptor( dbColumnDescriptor );
        metaverseBuilder.addNode( dbColumnNode );

        // Get the stream field output from this step. It should've already been created when we called super.analyze()
        IComponentDescriptor transFieldDescriptor = getStepFieldOriginDescriptor( descriptor, fieldName );
        IMetaverseNode outNode = createNodeFromDescriptor( transFieldDescriptor );

        metaverseBuilder.addLink( dbColumnNode, DictionaryConst.LINK_POPULATES, outNode );

        // add a link from the fileField to the text file input step node
        metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, dbColumnNode );
      }
    }

    return rootNode;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( TableInputMeta.class );
      }
    };
  }

}

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

package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.HashSet;
import java.util.Set;

/**
 * The TableOutputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and
 * other metaverse entities (such as physical fields and tables)
 */
public class TableOutputStepAnalyzer extends BaseStepAnalyzer<TableOutputMeta> {

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IAnalyzer#analyze(IMetaverseComponentDescriptor,java.lang.Object)
   */
  @Override
  public IMetaverseNode analyze( IMetaverseComponentDescriptor descriptor, TableOutputMeta tableOutputMeta )
    throws MetaverseAnalyzerException {

    if ( tableOutputMeta == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.TableOutputMeta.IsNull" ) );
    }

    // Do common analysis for all step
    IMetaverseNode node = super.analyze( descriptor, tableOutputMeta );

    String tableName = tableOutputMeta.getTableName();

    String[] fieldNames = tableOutputMeta.getFieldStream();
    if ( fieldNames == null || fieldNames.length <= 0 || !tableOutputMeta.specifyFields() ) {
      // If no incoming fields are specified, get them from the previous step
      // NOTE: This check depends on the guarantee that super.loadInputAndOutputStreamFields() has been called.
      //  it is not done again here for performance purposes. Currently it's being called during super.analyze()
      if ( prevFields != null ) {
        fieldNames = prevFields.getFieldNames();
      }
    }

    String[] dbFieldNames = tableOutputMeta.getFieldDatabase();

    if ( tableName != null ) {

      // TODO The table is a child of the database connection. Right now we need to count on the fact that
      // TODO the database's ID will be of the form we expect. We will likely refactor ID generation.
      INamespace dbMetaNamespace = getSiblingNamespace(
          descriptor, tableOutputMeta.getDatabaseMeta().getName(), DictionaryConst.NODE_TYPE_DATASOURCE );
      IMetaverseComponentDescriptor dbTableDescriptor =
          getChildComponentDescriptor( dbMetaNamespace, tableName, DictionaryConst.NODE_TYPE_DATA_TABLE );
      IMetaverseNode tableNode = createNodeFromDescriptor( dbTableDescriptor );
      metaverseBuilder.addNode( tableNode );

      metaverseBuilder.addLink( node, DictionaryConst.LINK_WRITESTO, tableNode );

      if ( dbFieldNames == null || dbFieldNames.length == 0 || !tableOutputMeta.specifyFields() ) {
        // If no field names are specified, then all the incoming fields are written out by name verbatim
        dbFieldNames = fieldNames;
      }

      for ( int i = 0; i < fieldNames.length; i++ ) {
        String fieldName = fieldNames[i];

        // We can't use our own descriptor here, we need to get the descriptor for the origin step
        IMetaverseNode fieldNode =
            createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, fieldName ) );
        metaverseBuilder.addNode( fieldNode );

        if ( dbFieldNames != null ) {

          IMetaverseComponentDescriptor dbColumnDescriptor =
              getChildComponentDescriptor( dbTableDescriptor, dbFieldNames[i], DictionaryConst.NODE_TYPE_DATA_COLUMN );
          IMetaverseNode dbFieldNode = createNodeFromDescriptor( dbColumnDescriptor );

          metaverseBuilder.addNode( dbFieldNode );
          metaverseBuilder.addLink( fieldNode, DictionaryConst.LINK_READBY, rootNode );
          metaverseBuilder.addLink( fieldNode, DictionaryConst.LINK_POPULATES, dbFieldNode );
          metaverseBuilder.addLink( tableNode, DictionaryConst.LINK_CONTAINS, dbFieldNode );
        }
      }
    }

    return rootNode;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( TableOutputMeta.class );
      }
    };
  }
}

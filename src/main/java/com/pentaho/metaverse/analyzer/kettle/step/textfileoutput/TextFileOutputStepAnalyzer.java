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

package com.pentaho.metaverse.analyzer.kettle.step.textfileoutput;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;
import org.pentaho.platform.api.metaverse.MetaverseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * The TextFileOutputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the
 * fields operated on by Text File Output steps.
 */
public class TextFileOutputStepAnalyzer extends BaseStepAnalyzer<TextFileOutputMeta> {

  private Logger log = LoggerFactory.getLogger( TextFileOutputStepAnalyzer.class );

  @Override
  public IMetaverseNode analyze( IMetaverseComponentDescriptor descriptor, TextFileOutputMeta meta )
    throws MetaverseAnalyzerException {

    // do the common analysis...
    super.analyze( descriptor, meta );

    if ( !meta.isFileNameInField() ) {
      addStaticFileNodeAndLink( descriptor, meta );
    } else {
      addUsesLinkForStreamFileNameField( descriptor, meta );
    }
    // TODO: handle runtime file splitting for every X number of rows.
    // TODO: handle runtime "Accepts filename from field" - row level, dynamic file metadata

    addFieldNodesAndLinks( descriptor, meta );

    return rootNode;

  }

  protected void addStaticFileNodeAndLink( IMetaverseComponentDescriptor descriptor, TextFileOutputMeta meta ) {
    // get the file(s) that are being written to
    String[] files = meta.getFiles( meta.getParentStepMeta().getParentTransMeta() );

    // create node(s) form them
    for ( String file : files ) {
      if ( !Const.isEmpty( file ) ) {
        try {
          IMetaverseNode fileNode = createFileNode( file, descriptor );
          metaverseBuilder.addNode( fileNode );
          // add 'writesto' links to them from the rootNode
          metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_WRITESTO, fileNode );
        } catch ( MetaverseException e ) {
          log.error( e.getMessage(), e );
        }
      }
    }
  }

  protected void addUsesLinkForStreamFileNameField( IMetaverseComponentDescriptor descriptor,
                                                    TextFileOutputMeta meta ) {

    ValueMetaInterface filenameField = prevFields.searchValueMeta( meta.getFileNameField() );
    if ( filenameField != null ) {
      IMetaverseComponentDescriptor fileStreamFieldDescriptor = getStepFieldOriginDescriptor( descriptor,
        filenameField.getName() );
      IMetaverseNode fileStreamFieldNode = createNodeFromDescriptor( fileStreamFieldDescriptor );
      metaverseBuilder.addNode( fileStreamFieldNode );
      metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, fileStreamFieldNode );
    }
  }

  protected void addFieldNodesAndLinks( IMetaverseComponentDescriptor descriptor, TextFileOutputMeta meta ) {
    TextFileField[] outputFields = meta.getOutputFields();
    for ( TextFileField outputField : outputFields ) {
      String fieldName = outputField.getName();
      IMetaverseComponentDescriptor fileFieldDescriptor = new MetaverseComponentDescriptor(
        fieldName,
        DictionaryConst.NODE_TYPE_FILE_FIELD,
        descriptor,
        descriptor.getContext() );

      // create the file field nodes
      IMetaverseNode fieldNode = createNodeFromDescriptor( fileFieldDescriptor );
      metaverseBuilder.addNode( fieldNode );

      IMetaverseComponentDescriptor transFieldDescriptor = getStepFieldOriginDescriptor( descriptor, fieldName );
      IMetaverseNode transFieldNode = createNodeFromDescriptor( transFieldDescriptor );

      // add 'populates' links from the stream fields to them
      metaverseBuilder.addLink( transFieldNode, DictionaryConst.LINK_POPULATES, fieldNode );

      // add the links for "uses" stream fields
      metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, transFieldNode );
    }
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( TextFileOutputMeta.class );
      }
    };
  }
}

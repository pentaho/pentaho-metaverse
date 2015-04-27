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

package com.pentaho.metaverse.analyzer.kettle.step.getxmldata;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.MetaverseException;
import com.pentaho.metaverse.api.analyzer.kettle.step.BaseStepAnalyzer;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataField;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * The GetXMLDataStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and
 * other metaverse entities
 */
public class GetXMLDataStepAnalyzer extends BaseStepAnalyzer<GetXMLDataMeta> {
  private Logger log = LoggerFactory.getLogger( GetXMLDataStepAnalyzer.class );

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, GetXMLDataMeta getXMLDataMeta )
    throws MetaverseAnalyzerException {

    // do the common analysis for all step
    IMetaverseNode node = super.analyze( descriptor, getXMLDataMeta );

    // Add the XPath Loop to the step node
    node.setProperty( "loopXPath", getXMLDataMeta.getLoopXPath() );

    // add the fields as nodes, add the links too
    GetXMLDataField[] fields = getXMLDataMeta.getInputFields();
    if ( fields != null ) {
      for ( GetXMLDataField field : fields ) {
        String fieldName = field.getName();
        IComponentDescriptor fileFieldDescriptor = new MetaverseComponentDescriptor(
          fieldName,
          DictionaryConst.NODE_TYPE_FILE_FIELD,
          descriptor.getNamespace(),
          descriptor.getContext() );
        IMetaverseNode fieldNode = createNodeFromDescriptor( fileFieldDescriptor );

        // Add field properties like XPath
        fieldNode.setProperty( "xpath", field.getXPath() );
        fieldNode.setProperty( "element", field.getElementTypeCode() );
        fieldNode.setProperty( "resultType", field.getResultTypeCode() );
        fieldNode.setProperty( "repeat", field.isRepeated() );
        metaverseBuilder.addNode( fieldNode );

        // Get the stream field output from this step. It should've already been created when we called super.analyze()
        IComponentDescriptor transFieldDescriptor = getStepFieldOriginDescriptor( descriptor, fieldName );
        IMetaverseNode outNode = createNodeFromDescriptor( transFieldDescriptor );

        metaverseBuilder.addLink( fieldNode, DictionaryConst.LINK_POPULATES, outNode );

        // add a link from the fileField to the text file input step node
        metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, fieldNode );
      }
    }

    if ( getXMLDataMeta.isInFields() && getXMLDataMeta.getIsAFile() ) {
      String acceptingFieldName = getXMLDataMeta.getXMLField();
      IComponentDescriptor transFieldDescriptor = getPrevStepFieldOriginDescriptor( descriptor,
        acceptingFieldName );
      IMetaverseNode acceptingFieldNode = createNodeFromDescriptor( transFieldDescriptor );
      acceptingFieldNode.setProperty( "url", getXMLDataMeta.isReadUrl() );

      // add a link from the fileField to the text file input step node
      metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, acceptingFieldNode );
    } else {
      String[] fileNames = parentTransMeta.environmentSubstitute( getXMLDataMeta.getFileName() );

      // add a link from the file(s) being read to the step
      if ( fileNames != null ) {
        for ( String fileName : fileNames ) {
          if ( !Const.isEmpty( fileName ) ) {
            try {
              // first add the node for the file
              IMetaverseNode xmlFileNode = createFileNode( fileName, descriptor );
              metaverseBuilder.addNode( xmlFileNode );

              metaverseBuilder.addLink( xmlFileNode, DictionaryConst.LINK_READBY, node );
            } catch ( MetaverseException e ) {
              log.error( e.getMessage(), e );
            }
          }
        }
      }
    }

    return node;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( GetXMLDataMeta.class );
      }
    };
  }
}

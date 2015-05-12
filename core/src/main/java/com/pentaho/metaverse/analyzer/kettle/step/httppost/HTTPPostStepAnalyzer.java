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

package com.pentaho.metaverse.analyzer.kettle.step.httppost;

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.core.Const;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.httppost.HTTPPOSTMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.analyzer.kettle.step.BaseStepAnalyzer;

/**
 * The HTTPPostStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and other
 * metaverse entities
 */
public class HTTPPostStepAnalyzer extends BaseStepAnalyzer<HTTPPOSTMeta> {
  private Logger log = LoggerFactory.getLogger( HTTPPostStepAnalyzer.class );

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, HTTPPOSTMeta httpMeta )
    throws MetaverseAnalyzerException {

    // do the common analysis for all step
    IMetaverseNode node = super.analyze( descriptor, httpMeta );

    // add the fields as nodes, add the links too
    String[] fields = { httpMeta.getUrlField() };
    if ( fields != null && fields.length > 0 && httpMeta.isUrlInField() ) {
      for ( String field : fields ) {

        IComponentDescriptor urlFieldDescriptor =
            new MetaverseComponentDescriptor( field, DictionaryConst.NODE_TYPE_WEBSERVICE, descriptor.getNamespace(),
                descriptor.getContext() );
        IMetaverseNode fieldNode = createNodeFromDescriptor( urlFieldDescriptor );
        metaverseBuilder.addNode( fieldNode );
        metaverseBuilder.addLink( fieldNode, DictionaryConst.LINK_READBY, node );
      }
    }

    String[] argumentFields = httpMeta.getArgumentField();
    if ( argumentFields != null ) {
      for ( String argumentField : argumentFields ) {
        IComponentDescriptor argumentFieldDescriptor = getPrevStepFieldOriginDescriptor( descriptor, argumentField );
        IMetaverseNode argumentFieldNode = createNodeFromDescriptor( argumentFieldDescriptor );
        metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, argumentFieldNode );
      }
    }

    String[] headerFields = httpMeta.getArgumentField();
    if ( headerFields != null ) {
      for ( String headerField : headerFields ) {
        IComponentDescriptor headerFieldDescriptor = getPrevStepFieldOriginDescriptor( descriptor, headerField );
        IMetaverseNode headerFieldNode = createNodeFromDescriptor( headerFieldDescriptor );
        metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, headerFieldNode );
      }
    }

    if ( httpMeta.isUrlInField() ) {
      String acceptingFieldName = httpMeta.getUrlField();
      IComponentDescriptor transFieldDescriptor = getPrevStepFieldOriginDescriptor( descriptor, acceptingFieldName );
      IMetaverseNode acceptingFieldNode = createNodeFromDescriptor( transFieldDescriptor );

      // add a link from the fileField to the text file input step node
      metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, acceptingFieldNode );
    } else {
      String[] urls = { httpMeta.getUrl() };

      // add a link from the url being read to the step
      if ( urls != null ) {
        for ( String url : urls ) {
          if ( !Const.isEmpty( url ) ) {
            // first add the node for the file
            IComponentDescriptor urlFieldDescriptor =
                new MetaverseComponentDescriptor( url, DictionaryConst.NODE_TYPE_WEBSERVICE, descriptor.getNamespace(),
                    descriptor.getContext() );
            IMetaverseNode fieldNode = createNodeFromDescriptor( urlFieldDescriptor );
            metaverseBuilder.addNode( fieldNode );
            metaverseBuilder.addLink( fieldNode, DictionaryConst.LINK_READBY, node );
          }
        }
      }
    }

    String[] argumentField = httpMeta.getArgumentField();
    String[] argumentParameter = httpMeta.getArgumentParameter();
    System.out.println( argumentField );
    System.out.println( argumentParameter );

    return node;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( HTTPPOSTMeta.class );
      }
    };
  }
}

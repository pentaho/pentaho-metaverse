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

package com.pentaho.metaverse.analyzer.kettle.step.rest;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.analyzer.kettle.step.BaseStepAnalyzer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.trans.step.BaseStepMeta;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.di.trans.steps.rest.RestMeta;

import java.util.HashSet;
import java.util.Set;

public class RestClientStepAnalyzer extends BaseStepAnalyzer<RestMeta> {

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, RestMeta stepMeta )
    throws MetaverseAnalyzerException {

    IMetaverseNode stepNode = super.analyze( descriptor, stepMeta );

    handleIsReadByLinks( descriptor, stepMeta, stepNode );
    handleUsesFields( descriptor, stepMeta, stepNode );

    return stepNode;
  }

  private void handleIsReadByLinks( IComponentDescriptor descriptor, RestMeta stepMeta, IMetaverseNode stepNode ) {
    String url = null;
    if ( stepMeta.isUrlInField() ) {
      url = stepMeta.getUrlField();
    } else {
      // TODO: do we need to resolve the params/variables?
      url = parentTransMeta.environmentSubstitute( stepMeta.getUrl() );
    }

    MetaverseComponentDescriptor componentDescriptor = new MetaverseComponentDescriptor(
      url, DictionaryConst.NODE_TYPE_WEBSERVICE, descriptor.getNamespace(), descriptor.getContext()
    );
    IMetaverseNode nodeFromDescriptor = createNodeFromDescriptor( componentDescriptor );
    metaverseBuilder.addNode( nodeFromDescriptor );
    metaverseBuilder.addLink( nodeFromDescriptor, DictionaryConst.LINK_READBY, stepNode );
  }

  private void handleUsesFields( IComponentDescriptor descriptor, RestMeta stepMeta, IMetaverseNode stepNode ) {
    Set<String> usedFields = new HashSet<>();

    // add url field
    if ( stepMeta.isUrlInField() && StringUtils.isNotEmpty( stepMeta.getUrlField() ) ) {
      usedFields.add( stepMeta.getUrlField() );
    }

    // add method field
    if ( stepMeta.isDynamicMethod() && StringUtils.isNotEmpty( stepMeta.getMethodFieldName() ) ) {
      usedFields.add( stepMeta.getMethodFieldName() );
    }

    // add body field
    if ( StringUtils.isNotEmpty( stepMeta.getBodyField() ) ) {
      usedFields.add( stepMeta.getBodyField() );
    }

    // add parameters as used fields
    String[] parameterFields = stepMeta.getParameterField();
    if ( ArrayUtils.isNotEmpty( parameterFields ) ) {
      for ( String paramField : parameterFields ) {
        usedFields.add( paramField );
      }
    }

    // add headers as used fields
    String[] headerFields = stepMeta.getHeaderField();
    if ( ArrayUtils.isNotEmpty( headerFields ) ) {
      for ( String headerField : headerFields ) {
        usedFields.add( headerField );
      }
    }

    for ( String usedField : usedFields ) {
      IComponentDescriptor fieldOriginDescriptor = getPrevStepFieldOriginDescriptor( descriptor, usedField );
      if ( fieldOriginDescriptor != null ) {
        IMetaverseNode nodeFromDescriptor = createNodeFromDescriptor( fieldOriginDescriptor );
        metaverseBuilder.addLink( stepNode, DictionaryConst.LINK_USES, nodeFromDescriptor );
      }
    }

  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<>();
    supportedSteps.add( RestMeta.class );
    return supportedSteps;
  }

}

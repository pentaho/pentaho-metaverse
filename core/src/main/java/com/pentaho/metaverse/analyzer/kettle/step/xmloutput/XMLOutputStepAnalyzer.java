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

package com.pentaho.metaverse.analyzer.kettle.step.xmloutput;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.xmloutput.XMLField;
import org.pentaho.di.trans.steps.xmloutput.XMLOutputMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * The XMLOutputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the
 * fields operated on by XML Output steps.
 */
public class XMLOutputStepAnalyzer extends ExternalResourceStepAnalyzer<XMLOutputMeta> {

  private Logger log = LoggerFactory.getLogger( XMLOutputStepAnalyzer.class );

  @Override
  protected Set<StepField> getUsedFields( XMLOutputMeta meta ) {
    return null;
  }

  @Override protected void customAnalyze( XMLOutputMeta meta, IMetaverseNode node ) throws MetaverseAnalyzerException {
    super.customAnalyze( meta, node );
    node.setProperty( "parentnode", meta.getMainElement() );
    node.setProperty( "rownode", meta.getRepeatElement() );
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( XMLOutputMeta.class );
      }
    };
  }

  @Override
  public IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException {
    return createFileNode( resource.getName(), getDescriptor() );
  }

  @Override
  public String getResourceInputNodeType() {
    return null;
  }

  @Override
  public String getResourceOutputNodeType() {
    return DictionaryConst.NODE_TYPE_FILE_FIELD;
  }

  @Override
  public boolean isOutput() {
    return true;
  }

  @Override
  public boolean isInput() {
    return false;
  }

  @Override
  public Set<String> getOutputResourceFields( XMLOutputMeta meta ) {
    Set<String> fields = new HashSet<>();
    XMLField[] outputFields = meta.getOutputFields();
    for ( int i = 0; i < outputFields.length; i++ ) {
      XMLField outputField = outputFields[ i ];
      fields.add( outputField.getFieldName() );
    }
    return fields;
  }

  ///////////// for unit testing
  protected void setBaseStepMeta( XMLOutputMeta meta ) {
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
  protected void setObjectFactory( IMetaverseObjectFactory objectFactory ) {
    this.metaverseObjectFactory = objectFactory;
  }
}

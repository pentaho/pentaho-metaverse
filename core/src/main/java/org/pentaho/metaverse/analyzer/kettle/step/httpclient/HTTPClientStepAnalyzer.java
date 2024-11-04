/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.step.httpclient;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.http.HTTPMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The HTTPClientStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and other
 * metaverse entities
 */
public class HTTPClientStepAnalyzer extends ExternalResourceStepAnalyzer<HTTPMeta> {
  private Logger log = LoggerFactory.getLogger( HTTPClientStepAnalyzer.class );

  @Override
  protected Set<StepField> getUsedFields( HTTPMeta stepMeta ) {
    Set<StepField> usedFields = new HashSet<>();

    // add url field
    if ( stepMeta.isUrlInField() && StringUtils.isNotEmpty( stepMeta.getUrlField() ) ) {
      usedFields.addAll( createStepFields( stepMeta.getUrlField(), getInputs() ) );
    }

    // add parameters as used fields
    String[] parameterFields = stepMeta.getArgumentField();
    if ( ArrayUtils.isNotEmpty( parameterFields ) ) {
      for ( String paramField : parameterFields ) {
        usedFields.addAll( createStepFields( paramField, getInputs() ) );
      }
    }

    // add headers as used fields
    String[] headerFields = stepMeta.getHeaderField();
    if ( ArrayUtils.isNotEmpty( headerFields ) ) {
      for ( String headerField : headerFields ) {
        usedFields.addAll( createStepFields( headerField, getInputs() ) );
      }
    }

    return usedFields;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( HTTPMeta.class );
      }
    };
  }

  @Override
  protected Map<String, RowMetaInterface> getInputRowMetaInterfaces( HTTPMeta meta ) {
    return getInputFields( meta );
  }

  @Override public IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException {
    MetaverseComponentDescriptor componentDescriptor = new MetaverseComponentDescriptor(
      resource.getName(), getResourceInputNodeType(), descriptor.getNamespace(), descriptor.getContext()
    );
    IMetaverseNode node = createNodeFromDescriptor( componentDescriptor );
    return node;
  }

  @Override public String getResourceInputNodeType() {
    return DictionaryConst.NODE_TYPE_WEBSERVICE;
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

  ////// used in unit testing
  protected void setObjectFactory( IMetaverseObjectFactory objectFactory ) {
    this.metaverseObjectFactory = objectFactory;
  }

  @Override
  public IClonableStepAnalyzer newInstance() {
    return new HTTPClientStepAnalyzer();
  }
}

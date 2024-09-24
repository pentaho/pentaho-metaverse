/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.analyzer.kettle.step.httppost;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.httppost.HTTPPOSTMeta;
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
 * The HTTPPostStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and other
 * metaverse entities
 */
public class HTTPPostStepAnalyzer extends ExternalResourceStepAnalyzer<HTTPPOSTMeta> {
  private Logger log = LoggerFactory.getLogger( HTTPPostStepAnalyzer.class );


  @Override
  protected Set<StepField> getUsedFields( HTTPPOSTMeta stepMeta ) {
    Set<StepField> usedFields = new HashSet<>();

    // add url field
    if ( stepMeta.isUrlInField() && StringUtils.isNotEmpty( stepMeta.getUrlField() ) ) {
      usedFields.addAll( createStepFields( stepMeta.getUrlField(), getInputs() ) );
    }

    // add parameters as used fields
    String[] parameterFields = stepMeta.getQueryField();
    if ( ArrayUtils.isNotEmpty( parameterFields ) ) {
      for ( String paramField : parameterFields ) {
        usedFields.addAll( createStepFields( paramField, getInputs() ) );
      }
    }

    // add headers as used fields
    String[] headerFields = stepMeta.getArgumentField();
    if ( ArrayUtils.isNotEmpty( headerFields ) ) {
      for ( String headerField : headerFields ) {
        usedFields.addAll( createStepFields( headerField, getInputs() ) );
      }
    }
    return usedFields;
  }
  @Override
  protected Map<String, RowMetaInterface> getInputRowMetaInterfaces( HTTPPOSTMeta meta ) {
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
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( HTTPPOSTMeta.class );
      }
    };
  }

  @Override
  public IClonableStepAnalyzer newInstance() {
    return new HTTPPostStepAnalyzer();
  }
}

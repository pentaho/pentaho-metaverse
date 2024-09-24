/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.ArrayUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.httppost.HTTPPOST;
import org.pentaho.di.trans.steps.httppost.HTTPPOSTMeta;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.WebServiceResourceInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class HTTPPostExternalResourceConsumer
  extends BaseStepExternalResourceConsumer<HTTPPOST, HTTPPOSTMeta> {

  private static HTTPPostExternalResourceConsumer instance;

  @VisibleForTesting
  protected HTTPPostExternalResourceConsumer() {
  }

  public static HTTPPostExternalResourceConsumer getInstance() {
    if ( null == instance ) {
      instance = new HTTPPostExternalResourceConsumer();
    }
    return instance;
  }

  @Override
  public boolean isDataDriven( HTTPPOSTMeta meta ) {
    // We can safely assume that the StepMetaInterface object we get back is a TextFileInputMeta
    return true;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( HTTPPOSTMeta meta, IAnalysisContext context ) {
    Collection<IExternalResourceInfo> resources = Collections.emptyList();

    // We only need to collect these resources if we're not data-driven and there are no used variables in the
    // metadata relating to external files.
    if ( !meta.isUrlInField() /* TODO */ ) {
      StepMeta parentStepMeta = meta.getParentStepMeta();
      if ( parentStepMeta != null ) {
        TransMeta parentTransMeta = parentStepMeta.getParentTransMeta();
        if ( parentTransMeta != null ) {
          String[] urls = { meta.getUrl() };
          if ( urls != null ) {
            resources = new ArrayList<IExternalResourceInfo>( urls.length );

            for ( String url : urls ) {
              if ( !Const.isEmpty( url ) ) {
                try {

                  IExternalResourceInfo resource = ExternalResourceInfoFactory
                    .createURLResource( url, true );
                  if ( resource != null ) {
                    resources.add( resource );
                  } else {
                    throw new KettleFileException( "Error getting file resource!" );
                  }
                } catch ( KettleFileException kfe ) {
                  // TODO throw or ignore?
                }
              }
            }
          }
        }
      }
    }
    return resources;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromRow(
    HTTPPOST httpClientInput, RowMetaInterface rowMeta, Object[] row ) {
    Collection<IExternalResourceInfo> resources = new LinkedList<IExternalResourceInfo>();

    // For some reason the step doesn't return the StepMetaInterface directly, so go around it
    HTTPPOSTMeta meta = (HTTPPOSTMeta) httpClientInput.getStepMetaInterface();
    if ( meta == null ) {
      meta = (HTTPPOSTMeta) httpClientInput.getStepMeta().getStepMetaInterface();
    }
    if ( meta != null ) {
      try {
        String url;
        if ( meta.isUrlInField() ) {
          url = rowMeta.getString( row, meta.getUrlField(), null );
        } else {
          url = meta.getUrl();
        }
        if ( !Const.isEmpty( url ) ) {
          WebServiceResourceInfo resourceInfo =
            (WebServiceResourceInfo) ExternalResourceInfoFactory.createURLResource( url, true );

          if ( ArrayUtils.isNotEmpty( meta.getArgumentField() ) ) {
            for ( int i = 0; i < meta.getArgumentField().length; i++ ) {
              String field = meta.getArgumentField()[ i ];
              String label = meta.getArgumentParameter()[ i ];
              resourceInfo.addHeader( label, rowMeta.getString( row, field, null ) );
            }
          }

          if ( ArrayUtils.isNotEmpty( meta.getQueryField() ) ) {
            for ( int i = 0; i < meta.getQueryField().length; i++ ) {
              String field = meta.getQueryField()[ i ];
              String label = meta.getQueryParameter()[ i ];
              resourceInfo.addParameter( label, rowMeta.getString( row, field, null ) );
            }
          }

          resources.add( resourceInfo );
        }
      } catch ( KettleException kve ) {
        // TODO throw exception or ignore?
      }
    }
    return resources;
  }

  @Override
  public Class<HTTPPOSTMeta> getMetaClass() {
    return HTTPPOSTMeta.class;
  }
}

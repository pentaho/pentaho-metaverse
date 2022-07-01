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

package org.pentaho.metaverse.analyzer.kettle.step.httpclient;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.ArrayUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.http.HTTP;
import org.pentaho.di.trans.steps.http.HTTPMeta;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.WebServiceResourceInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class HTTPClientExternalResourceConsumer
  extends BaseStepExternalResourceConsumer<HTTP, HTTPMeta> {

  private static HTTPClientExternalResourceConsumer instance;

  @VisibleForTesting
  protected HTTPClientExternalResourceConsumer() {
  }

  public static HTTPClientExternalResourceConsumer getInstance() {
    if ( null == instance ) {
      instance = new HTTPClientExternalResourceConsumer();
    }
    return instance;
  }

  @Override
  public boolean isDataDriven( HTTPMeta meta ) {
    // We can safely assume that this is always data driven
    return true;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( HTTPMeta meta, IAnalysisContext context ) {
    Collection<IExternalResourceInfo> resources = Collections.emptyList();

    // We only need to collect these resources if we're not getting the url from a field
    if ( !meta.isUrlInField()  ) {
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
    HTTP httpClientInput, RowMetaInterface rowMeta, Object[] row ) {
    Collection<IExternalResourceInfo> resources = new LinkedList<IExternalResourceInfo>();

    // For some reason the step doesn't return the StepMetaInterface directly, so go around it
    HTTPMeta meta = (HTTPMeta) httpClientInput.getStepMetaInterface();
    if ( meta == null ) {
      meta = (HTTPMeta) httpClientInput.getStepMeta().getStepMetaInterface();
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

          if ( ArrayUtils.isNotEmpty( meta.getHeaderField() ) ) {
            for ( int i = 0; i < meta.getHeaderField().length; i++ ) {
              String field = meta.getHeaderField()[ i ];
              String label = meta.getHeaderParameter()[ i ];
              resourceInfo.addHeader( label, rowMeta.getString( row, field, null ) );
            }
          }

          if ( ArrayUtils.isNotEmpty( meta.getArgumentField() ) ) {
            for ( int i = 0; i < meta.getArgumentField().length; i++ ) {
              String field = meta.getArgumentField()[ i ];
              String label = meta.getArgumentParameter()[ i ];
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
  public Class<HTTPMeta> getMetaClass() {
    return HTTPMeta.class;
  }
}

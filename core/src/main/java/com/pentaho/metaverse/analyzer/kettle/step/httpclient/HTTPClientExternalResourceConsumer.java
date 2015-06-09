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

package com.pentaho.metaverse.analyzer.kettle.step.httpclient;

import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.WebServiceResourceInfo;
import org.apache.commons.lang.ArrayUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.http.HTTP;
import org.pentaho.di.trans.steps.http.HTTPMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class HTTPClientExternalResourceConsumer
  extends BaseStepExternalResourceConsumer<HTTP, HTTPMeta> {

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

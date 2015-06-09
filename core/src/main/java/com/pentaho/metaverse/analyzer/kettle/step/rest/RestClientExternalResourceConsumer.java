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

import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.WebServiceResourceInfo;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.rest.Rest;
import org.pentaho.di.trans.steps.rest.RestMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by rfellows on 5/11/15.
 */
public class RestClientExternalResourceConsumer extends BaseStepExternalResourceConsumer<Rest, RestMeta> {

  private Logger log = LoggerFactory.getLogger( RestClientExternalResourceConsumer.class );

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( RestMeta meta, IAnalysisContext context ) {

    List<IExternalResourceInfo> resources = new ArrayList<>();

    if ( !meta.isUrlInField() ) {
      String url = meta.getUrl();

      WebServiceResourceInfo resourceInfo = createResourceInfo( url, meta );
      resources.add( resourceInfo );
    }

    return resources;
  }

  private WebServiceResourceInfo createResourceInfo( String url, RestMeta meta ) {

    WebServiceResourceInfo resourceInfo =
      (WebServiceResourceInfo) ExternalResourceInfoFactory.createURLResource( url, true );

    if ( !meta.isDynamicMethod() ) {
      resourceInfo.setMethod( meta.getMethod() );
    }
    if ( StringUtils.isNotEmpty( meta.getBodyField() ) ) {
      resourceInfo.setBody( meta.getBodyField() );
    }
    resourceInfo.setApplicationType( meta.getApplicationType() );
    return resourceInfo;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromRow( Rest step, RowMetaInterface rowMeta, Object[] row ) {
    Set<IExternalResourceInfo> resources = new HashSet<>();

    RestMeta meta = (RestMeta) step.getStepMetaInterface();
    if ( meta == null ) {
      meta = (RestMeta) step.getStepMeta().getStepMetaInterface();
    }

    if ( meta != null ) {
      String url;
      String method;
      String body;

      try {
        if ( meta.isUrlInField() ) {
          url = rowMeta.getString( row, meta.getUrlField(), null );
        } else {
          url = meta.getUrl();
        }
        if ( StringUtils.isNotEmpty( url ) ) {
          WebServiceResourceInfo resourceInfo = createResourceInfo( url, meta );
          if ( ArrayUtils.isNotEmpty( meta.getHeaderField() ) ) {
            for ( int i = 0; i < meta.getHeaderField().length; i++ ) {
              String field = meta.getHeaderField()[ i ];
              String label = meta.getHeaderName()[ i ];
              resourceInfo.addHeader( label, rowMeta.getString( row, field, null ) );
            }
          }
          if ( ArrayUtils.isNotEmpty( meta.getParameterField() ) ) {
            for ( int i = 0; i < meta.getParameterField().length; i++ ) {
              String field = meta.getParameterField()[ i ];
              String label = meta.getParameterName()[ i ];
              resourceInfo.addParameter( label, rowMeta.getString( row, field, null ) );
            }
          }
          if ( meta.isDynamicMethod() ) {
            method = rowMeta.getString( row, meta.getMethodFieldName(), null );
            resourceInfo.setMethod( method );
          }

          if ( StringUtils.isNotEmpty( meta.getBodyField() ) ) {
            body = rowMeta.getString( row, meta.getBodyField(), null );
            resourceInfo.setBody( body );
          }

          resources.add( resourceInfo );
        }
      } catch ( KettleValueException e ) {
        // could not find a url on this row
        log.debug( e.getMessage(), e );
      }
    }
    return resources;
  }

  @Override
  public boolean isDataDriven( RestMeta meta ) {
    // this step is data driven no matter what.
    // either the url, method, body, headers, and/or parameters come from the previous step
    return true;
  }

  @Override
  public Class<RestMeta> getMetaClass() {
    return RestMeta.class;
  }
}

/*!
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

package com.pentaho.metaverse.api.testutils;

import com.pentaho.metaverse.api.IDocumentController;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseObjectFactory;
import com.pentaho.metaverse.api.MetaverseObjectFactory;
import com.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumerProvider;
import com.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumerProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author mburgess
 */
public class MetaverseTestUtils {

  private static IMetaverseObjectFactory metaverseObjectFactory = new MetaverseObjectFactory();

  public static IMetaverseObjectFactory getMetaverseObjectFactory() {
    return metaverseObjectFactory;
  }

  public static IDocumentController getDocumentController() {
    IDocumentController documentController = mock( IDocumentController.class );
    IMetaverseBuilder metaverseBuilder = mock( IMetaverseBuilder.class );
    when( metaverseBuilder.getMetaverseObjectFactory() ).thenReturn( getMetaverseObjectFactory() );
    when( documentController.getMetaverseObjectFactory() ).thenReturn( getMetaverseObjectFactory() );
    when( documentController.getMetaverseBuilder() ).thenReturn( metaverseBuilder );
    return documentController;
  }

  public static IStepExternalResourceConsumerProvider getStepExternalResourceConsumerProvider() {
    IStepExternalResourceConsumerProvider provider = mock( IStepExternalResourceConsumerProvider.class );
    // TODO
    return provider;
  }

  public static IJobEntryExternalResourceConsumerProvider getJobEntryExternalResourceConsumerProvider() {
    IJobEntryExternalResourceConsumerProvider provider = mock( IJobEntryExternalResourceConsumerProvider.class );
    // TODO
    return provider;
  }
}

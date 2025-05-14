/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.testutils;

import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseObjectFactory;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumerProvider;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumerProvider;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

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
    lenient().when( metaverseBuilder.getMetaverseObjectFactory() ).thenReturn( getMetaverseObjectFactory() );
    lenient().when( documentController.getMetaverseObjectFactory() ).thenReturn( getMetaverseObjectFactory() );
    lenient().when( documentController.getMetaverseBuilder() ).thenReturn( metaverseBuilder );
    return documentController;
  }

  public static IStepExternalResourceConsumerProvider getStepExternalResourceConsumerProvider() {
    // TODO
    return mock( IStepExternalResourceConsumerProvider.class );
  }

  public static IJobEntryExternalResourceConsumerProvider getJobEntryExternalResourceConsumerProvider() {
    // TODO
    return mock( IJobEntryExternalResourceConsumerProvider.class );
  }
}

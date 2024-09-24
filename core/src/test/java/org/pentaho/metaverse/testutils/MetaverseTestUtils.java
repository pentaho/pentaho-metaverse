/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

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


package org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.entry;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metaverse.api.analyzer.kettle.IExternalResourceConsumer;

import java.util.Collection;

import static org.junit.Assert.*;

public class BaseJobEntryExternalResourceConsumerTest {

  BaseJobEntryExternalResourceConsumer consumer;

  @Before
  public void setUp() throws Exception {
    consumer = new BaseJobEntryExternalResourceConsumer() {
      @Override
      public Class getMetaClass() {
        return null;
      }
    };
  }

  @Test
  public void testIsDataDriven() throws Exception {
    assertFalse( consumer.isDataDriven( null ) );
  }

  @Test
  public void testGetResourcesFromMeta() throws Exception {
    Collection<IExternalResourceConsumer> resources = consumer.getResourcesFromMeta( null );
    assertNotNull( resources );
    assertTrue( resources.isEmpty() );
  }

}

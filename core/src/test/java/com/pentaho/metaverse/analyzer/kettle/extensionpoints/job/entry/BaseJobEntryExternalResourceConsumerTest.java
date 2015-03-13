package com.pentaho.metaverse.analyzer.kettle.extensionpoints.job.entry;

import com.pentaho.metaverse.analyzer.kettle.extensionpoints.IExternalResourceConsumer;
import org.junit.Before;
import org.junit.Test;

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

package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class BaseStepExternalResourceConsumerTest {

  BaseStepExternalResourceConsumer consumer;

  @Before
  public void setUp() throws Exception {
    consumer = new BaseStepExternalResourceConsumer() {
      @Override
      public Class getStepMetaClass() {
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

  @Test
  public void testGetResourcesFromRow() throws Exception {
    Collection<IExternalResourceConsumer> resources = consumer.getResourcesFromRow( null, null, null );
    assertNotNull( resources );
    assertTrue( resources.isEmpty() );
  }
}

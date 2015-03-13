package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.trans.step.BaseStepMeta;

import static org.junit.Assert.*;

public class ExternalResourceConsumerMapTest {

  ExternalResourceConsumerMap map;

  @Before
  public void setUp() throws Exception {
    map = ExternalResourceConsumerMap.getInstance();
  }

  @Test
  public void testGetInstance() throws Exception {
    assertNotNull( map );
  }

  @Test
  public void testGetStepExternalResourceConsumers() throws Exception {
    assertNotNull( map.getStepExternalResourceConsumers( BaseStepMeta.class ) );
  }

  @Test
  public void testGetJobEntryExternalResourceConsumers() throws Exception {
    assertNotNull( map.getJobEntryExternalResourceConsumers( JobEntryBase.class ) );
  }

}

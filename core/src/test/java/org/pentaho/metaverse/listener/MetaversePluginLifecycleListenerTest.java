/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse.listener;

import com.tinkerpop.blueprints.Graph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class MetaversePluginLifecycleListenerTest {

  @Mock private Graph mockGraph;

  private MetaversePluginLifecycleListener metaversePluginLifecycleListener;

  @Before
  public void setUp() throws Exception {
    metaversePluginLifecycleListener = new MetaversePluginLifecycleListener();
  }

  @Test
  public void testUnload() throws Exception {
    metaversePluginLifecycleListener.setGraph( mockGraph );
    metaversePluginLifecycleListener.unLoaded();

    verify( mockGraph, times ( 1 ) ).shutdown();
  }

  @Test
  public void testUnload_NullGraph() throws Exception {
    MetaversePluginLifecycleListener spyListener = spy( metaversePluginLifecycleListener );
    doReturn( null ).when( spyListener ).getGraph();
    spyListener.unLoaded();

    verify( mockGraph, times ( 0 ) ).shutdown();
  }

  @Test
  public void testLoaded() throws Exception {
    metaversePluginLifecycleListener.loaded();
  }

  @Test
  public void testInit() throws Exception {
    metaversePluginLifecycleListener.init();
  }
}

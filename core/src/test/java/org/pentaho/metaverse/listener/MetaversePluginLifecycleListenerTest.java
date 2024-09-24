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

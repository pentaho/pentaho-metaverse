/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetaverseBeanUtilTest {

  public static final String BEAN_NAME = "testBean";

  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void testGetInstance() throws Exception {
    assertNotNull( MetaverseBeanUtil.getInstance() );
  }

  @Test
  public void testSetBundleContext() throws Exception {
    BundleContext bundleContext = mock( BundleContext.class );
    MetaverseBeanUtil.getInstance().setBundleContext( bundleContext );
  }

  @Test
  public void testGet() throws Exception {
    MetaverseBeanUtil instance = MetaverseBeanUtil.getInstance();
    BundleContext bundleContext = mock( BundleContext.class );
    Bundle bundle = mock( Bundle.class );
    when( bundleContext.getBundle() ).thenReturn( bundle );
    instance.setBundleContext( bundleContext );
    assertNull( instance.get( BEAN_NAME ) );

    ServiceReference serviceReference = mock( ServiceReference.class );
    when( bundleContext.getServiceReferences( Mockito.any( Class.class ), Mockito.anyString() ) )
      .thenReturn( Collections.singletonList( serviceReference ) );
    assertNull( instance.get( BEAN_NAME ) );

    BlueprintContainer service = mock( BlueprintContainer.class );
    when( bundleContext.getService( Mockito.any( ServiceReference.class ) ) ).thenReturn( service );
    Object testObject = new Object();
    when( service.getComponentInstance( BEAN_NAME ) ).thenReturn( testObject );
    assertEquals( testObject, instance.get( BEAN_NAME ) );

    when( bundleContext.getServiceReferences( Mockito.any( Class.class ), Mockito.anyString() ) )
      .thenThrow( InvalidSyntaxException.class );
    assertNull( MetaverseBeanUtil.getInstance().get( BEAN_NAME ) );


  }
}

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

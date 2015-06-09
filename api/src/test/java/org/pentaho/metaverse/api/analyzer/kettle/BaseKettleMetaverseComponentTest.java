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
package org.pentaho.metaverse.api.analyzer.kettle;

import org.junit.Test;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseObjectFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class BaseKettleMetaverseComponentTest {

  BaseKettleMetaverseComponent component = new BaseKettleMetaverseComponent() {
  };

  @Test
  public void testGetMetaverseBuilder() throws Exception {
    assertNull( component.getMetaverseBuilder() );
    component.metaverseBuilder = mock( IMetaverseBuilder.class );
    assertNotNull( component.getMetaverseBuilder() );
  }

  @Test
  public void testGetSiblingNamespace() throws Exception {
    INamespace namespace = mock( INamespace.class );

    // be sure we don't call through to the underlying namespace object if it is null
    assertNull( component.getSiblingNamespace( null, null, null ) );
    verify( namespace, times( 0 ) ).getSiblingNamespace( anyString(), anyString() );

    // verify the delegate methods are called
    component.getSiblingNamespace( namespace, null, null );
    verify( namespace, times( 1 ) ).getSiblingNamespace( null, null );

    component.getSiblingNamespace( namespace, "name", null );
    verify( namespace, times( 1 ) ).getSiblingNamespace( "name", null );

    component.getSiblingNamespace( namespace, "name", "type" );
    verify( namespace, times( 1 ) ).getSiblingNamespace( "name", "type" );
  }

  @Test
  public void testCreateFileNode() throws Exception {
    component.createFileNode( null, null );
    assertNull( component.createFileNode( "/path/to/my/file", null ) );
    IMetaverseBuilder metaverseBuilder = mock( IMetaverseBuilder.class );
    when( metaverseBuilder.getMetaverseObjectFactory() ).thenReturn( new MetaverseObjectFactory() );
    component.setMetaverseBuilder( metaverseBuilder );

    IComponentDescriptor descriptor = mock( IComponentDescriptor.class );
    INamespace ns = mock( INamespace.class );
    when( descriptor.getNamespace() ).thenReturn( ns );
    assertNotNull( component.createFileNode( "/path/to/my/file", descriptor ) );
  }
}

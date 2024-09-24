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

package org.pentaho.metaverse.api.analyzer.kettle;

import org.junit.Test;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.ILogicalIdGenerator;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseObjectFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  @Test
  public void testCreateNodeFromDescriptor() throws Exception {
    String namespaceId = "{namespace: 'my namespace', name: 'my name', type: 'my type'}";
    ILogicalIdGenerator idGenerator = DictionaryConst.LOGICAL_ID_GENERATOR_DEFAULT;
    IComponentDescriptor descriptor = mock( IComponentDescriptor.class );
    INamespace ns = mock( INamespace.class );

    when( descriptor.getParentNamespace() ).thenReturn( mock( INamespace.class ) );
    when( descriptor.getNamespace() ).thenReturn( ns );
    when( ns.getNamespaceId() ).thenReturn( namespaceId );

    component.metaverseObjectFactory = spy( new MetaverseObjectFactory() );
    IMetaverseNode node = component.createNodeFromDescriptor( descriptor, idGenerator );

    assertNotNull( node );
    assertTrue( node.getLogicalId().contains( namespaceId ) );
  }
}

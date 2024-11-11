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


package org.pentaho.metaverse.api;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.dictionary.DictionaryConst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: RFellows Date: 8/15/14
 */
public class MetaverseComponentDescriptorTest {

  private MetaverseComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testConstructor() throws Exception {
    INamespace ns = null;
    descriptor = new MetaverseComponentDescriptor( null, null, ns );
    assertNull( descriptor.getType() );
    assertNull( descriptor.getName() );
    assertNull( descriptor.getNamespace() );
  }

  @Test
  public void testConstructor2() throws Exception {
    IMetaverseNode node = null;
    descriptor = new MetaverseComponentDescriptor( null, null, node );
    assertNull( descriptor.getType() );
    assertNull( descriptor.getName() );
    assertNull( descriptor.getNamespace() );
  }

  @Test
  public void testGettersSetters() throws Exception {
    INamespace mockEmptyNamespace = mock( INamespace.class );
    when( mockEmptyNamespace.getNamespaceId() ).thenReturn( null );

    INamespace mockNamespace = mock( INamespace.class );
    when( mockNamespace.getNamespaceId() ).thenReturn( "namespace-id" );
    when( mockNamespace.getSiblingNamespace( "any", "any" ) ).thenReturn( new Namespace( "my brother's namespace" ) );

    descriptor = new MetaverseComponentDescriptor( null, null, mockEmptyNamespace );
    assertNull( descriptor.getStringID() );
    assertNull( descriptor.getName() );
    assertNull( descriptor.getType() );
    assertEquals( mockEmptyNamespace, descriptor.getNamespace() );
    assertNotNull( descriptor.getContext() );
    assertEquals( descriptor.getContext().getContextName(), DictionaryConst.CONTEXT_DEFAULT );

    descriptor.setName( "test name" );
    assertEquals( "test name", descriptor.getName() );

    descriptor.setType( "test type" );
    assertEquals( "test type", descriptor.getType() );

    descriptor.setNamespace( mockNamespace );
    assertEquals( "namespace-id", descriptor.getStringID() );
    descriptor.getSiblingNamespace( "any", "any" );
    verify( mockNamespace ).getSiblingNamespace( eq( "any" ), eq( "any" ) );

    descriptor = new MetaverseComponentDescriptor( null, null, mockEmptyNamespace, null );
    assertNull( descriptor.getStringID() );
    assertNull( descriptor.getName() );
    assertNull( descriptor.getType() );
    assertEquals( mockEmptyNamespace, descriptor.getNamespace() );
    assertNull( descriptor.getContext() );
    descriptor.setContext( new AnalysisContext( "test context", null ) );
    IAnalysisContext context = descriptor.getContext();
    assertNotNull( context );
    assertEquals( "test context", context.getContextName() );
    assertNull( context.getContextObject() );

  }

  @Test
  public void testGetNullNamespace() throws Exception {
    INamespace mockEmptyNamespace = null;

    descriptor = new MetaverseComponentDescriptor( null, null, mockEmptyNamespace );
    assertNull( descriptor.getNamespaceId() );
  }

  @Test
  public void testSetLogicalIdGenerator() {
    // Call for coverage
    INamespace mockEmptyNamespace = null;
    descriptor = new MetaverseComponentDescriptor( null, null, mockEmptyNamespace );
    descriptor.setLogicalIdGenerator( null );
  }

}

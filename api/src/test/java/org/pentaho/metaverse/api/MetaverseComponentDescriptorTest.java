/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package org.pentaho.metaverse.api;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.dictionary.DictionaryConst;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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

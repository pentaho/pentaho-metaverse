/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

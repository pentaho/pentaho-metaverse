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

package org.pentaho.metaverse.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.MetaverseLink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith( MockitoJUnitRunner.class )
public class MetaverseLinkTest {

  @Mock
  IMetaverseNode fromNode;
  @Mock
  IMetaverseNode toNode;

  private MetaverseLink link;
  private MetaverseLink emptyLink;

  @Before
  public void before() {
    link = new MetaverseLink( fromNode, "uses", toNode );
    emptyLink = new MetaverseLink();
  }

  @Test
  public void testGetLabel() {
    assertEquals( "uses", link.getLabel() );
    assertNull( emptyLink.getLabel() );
    link.removeProperty( DictionaryConst.PROPERTY_LABEL );
    assertNull( link.getLabel() );
  }

  @Test
  public void testGetNodes() {
    assertEquals( fromNode, link.getFromNode() );
    assertEquals( toNode, link.getToNode() );
  }

  @Test
  public void getAndSetProperty() {
    assertNull( link.getProperty( "TEST" ) );
    link.setProperty( "TEST", "value" );
    assertEquals( "value", link.getProperty( "TEST" ) );
  }

  @Test
  public void getPropertyKeys() {
    assertEquals( 0, emptyLink.getPropertyKeys().size() );

    // label is a property, so it should be the initial prop in our test link
    assertEquals( 1, link.getPropertyKeys().size() );

    link.setProperty( "TEST", "value" );
    link.setProperty( "alias", "val" );
    assertEquals( 3, link.getPropertyKeys().size() );
  }

  @Test
  public void removeProperty() {
    link.setProperty( "TEST", "value" );
    link.setProperty( "alias", "val" );

    assertNull( link.removeProperty( "does not exist in props" ) );
    assertEquals( "value", link.removeProperty( "TEST" ) );
    assertNull( link.getProperty( "TEST" ) );
  }

}

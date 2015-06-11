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

package org.pentaho.metaverse.impl;

import org.junit.Test;
import org.mockito.Mock;
import org.pentaho.metaverse.api.IDocumentLocator;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class MetaverseDocumentLocationProviderTest {

  private MetaverseDocumentLocatorProvider provider;

  @Mock
  IDocumentLocator dummyLocator;

  @Test
  public void testAdd() {
    provider = new MetaverseDocumentLocatorProvider();
    assertEquals( 0, provider.getDocumentLocators().size() );
    provider.addDocumentLocator( dummyLocator );
    assertEquals( 1, provider.getDocumentLocators().size() );
  }

  @Test
  public void testRemove() {
    Set<IDocumentLocator> locators = new HashSet<IDocumentLocator>();
    locators.add( dummyLocator );
    provider = new MetaverseDocumentLocatorProvider( locators );
    assertEquals( 1, provider.getDocumentLocators().size() );

    provider.removeDocumentLocator( dummyLocator );
    assertEquals( 0, provider.getDocumentLocators().size() );

  }

}

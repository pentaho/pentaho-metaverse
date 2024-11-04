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

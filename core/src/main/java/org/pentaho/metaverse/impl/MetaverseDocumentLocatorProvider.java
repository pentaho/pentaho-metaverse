/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.metaverse.api.IDocumentLocator;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.locator.DIRepositoryLocator;

import java.util.HashSet;
import java.util.Set;

/**
 * Base implementation of the IDocumentLocatorProvider interface
 * @see IDocumentLocatorProvider
 */
public class MetaverseDocumentLocatorProvider implements IDocumentLocatorProvider {

  private Set<IDocumentLocator> documentLocators;

  private static MetaverseDocumentLocatorProvider instance;

  public static MetaverseDocumentLocatorProvider getInstance() {
    if ( null == instance ) {
      instance = new MetaverseDocumentLocatorProvider();
      instance.addDocumentLocator( new DIRepositoryLocator() );
    }
    return instance;
  }

  /**
   * Default constructor
   */
  MetaverseDocumentLocatorProvider() {
    documentLocators = new HashSet<>();
  }

  /**
   * Constructor that initializes with a provided set of locators
   * @param documentLocators locators to initialize with
   */
  @VisibleForTesting
  MetaverseDocumentLocatorProvider( Set<IDocumentLocator> documentLocators ) {
    setDocumentLocators( documentLocators );
  }

  @Override
  public Set<IDocumentLocator> getDocumentLocators() {
    return documentLocators;
  }

  /**
   * Set the DocumentLocators on the provider
   * @param documentLocators the locators
   */
  public void setDocumentLocators( Set<IDocumentLocator> documentLocators ) {
    this.documentLocators = documentLocators;
  }

  @Override
  public void addDocumentLocator( IDocumentLocator documentLocator ) {
    documentLocators.add( documentLocator );
  }

  @Override
  public boolean removeDocumentLocator( IDocumentLocator documentLocator ) {
    return documentLocators.remove( documentLocator );
  }





}

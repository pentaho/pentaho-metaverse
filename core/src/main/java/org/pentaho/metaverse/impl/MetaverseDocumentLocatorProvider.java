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

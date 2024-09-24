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

/**
 * IComponentDescriptor is a contract for an object that can describe a metaverse component. For example, it
 * could contain name, type, and namespace information for a particular document. The metadata about the component and
 * the component itself are separated to allow for maximum flexibility.
 */
public interface IComponentDescriptor extends IIdentifiable, INamespace {

  /**
   * Sets the namespace for the component described by this descriptor.
   *
   * @param namespace the namespace to set
   */
  void setNamespace( INamespace namespace );

  /**
   * Gets the namespace for the component described by this descriptor.
   *
   * @return the namespace of the described component
   */
  INamespace getNamespace();

  /**
   * Gets the context ("static", "runtime", e.g.) associated with the component described by this descriptor.
   *
   * @return An object containing the context information associated with the described component
   */
  IAnalysisContext getContext();

  /**
   * Sets the context ("static", "runtime", e.g.) associated with the component described by this descriptor.
   *
   * @param context the context for the described component
   */
  void setContext( IAnalysisContext context );
}

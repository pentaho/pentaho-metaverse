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

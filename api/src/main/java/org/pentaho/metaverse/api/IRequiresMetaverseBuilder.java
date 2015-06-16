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

/**
 * The IRequiresMetaverseBuilder contains helper methods for interfaces and classes that need a metaverse builder
 */
public interface IRequiresMetaverseBuilder {

  /**
   * Sets the metaverse builder used by analyzer(s) to create nodes and links in the metaverse
   * of content.
   *
   * @param builder the metaverse builder
   */
  void setMetaverseBuilder( IMetaverseBuilder builder );

  /**
   * Returns the metaverse builder used by analyzer(s) to create nodes and links in the metaverse
   * @return
   */
  IMetaverseBuilder getMetaverseBuilder();
}

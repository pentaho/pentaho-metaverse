/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import com.tinkerpop.blueprints.Graph;
import org.pentaho.metaverse.api.model.BaseMetaverseBuilder;

/**
 * This is the reference implementation for IMetaverseBuilder, offering the ability to add nodes, links, etc. to an
 * underlying graph
 */
public class MetaverseBuilder extends BaseMetaverseBuilder {
  /**
   * Instantiates a new Metaverse builder.
   *
   * @param graph the Graph to write to
   */
  public MetaverseBuilder( Graph graph ) {
    super( graph );
  }
}

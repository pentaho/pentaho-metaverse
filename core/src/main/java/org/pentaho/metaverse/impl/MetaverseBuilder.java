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

import com.tinkerpop.blueprints.Graph;
import org.pentaho.metaverse.api.model.BaseMetaverseBuilder;
import org.pentaho.metaverse.graph.SynchronizedGraphFactory;

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
  private static MetaverseBuilder instance;

  public static MetaverseBuilder getInstance() {
    if ( null == instance ) {
      instance = new MetaverseBuilder( SynchronizedGraphFactory.getDefaultGraph() );
    }
    return instance;
  }

  public MetaverseBuilder( Graph graph ) {
    super( graph );
  }

  public MetaverseBuilder() {
    super( SynchronizedGraphFactory.getDefaultGraph() );
  }
}

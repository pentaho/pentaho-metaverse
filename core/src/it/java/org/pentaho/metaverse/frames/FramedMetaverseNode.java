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

package org.pentaho.metaverse.frames;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import com.tinkerpop.frames.modules.javahandler.Initializer;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerClass;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import org.pentaho.dictionary.DictionaryConst;

import java.util.Set;

/**
 * User: RFellows Date: 9/4/14
 */
public interface FramedMetaverseNode extends VertexFrame {
  @Property( DictionaryConst.PROPERTY_NAME )
  String getName();

  @Property( DictionaryConst.PROPERTY_TYPE )
  String getType();

  @Property( "virtual" )
  Boolean isVirtual();

  @Property( DictionaryConst.PROPERTY_DESCRIPTION )
  String getDescription();

  @Adjacency( label = "contains", direction = Direction.OUT )
  Iterable<Concept> getContainedNodes();

  @Adjacency( label = "executes", direction = Direction.OUT )
  Iterable<TransformationNode> getExecutesNodes();

  @Adjacency( label = "populates", direction = Direction.OUT )
  Iterable<Concept> getNodesPopulatedByMe();

  @Adjacency( label = "typeconcept", direction = Direction.OUT )
  Iterable<Concept> getConcreteNodes();

  @GremlinGroovy( "it.out(linkType)" )
  Iterable<Concept> getOutNodes( @GremlinParam( "linkType" ) String linkType );

  @GremlinGroovy( "it.in(linkType)" )
  Iterable<Concept> getInNodes( @GremlinParam( "linkType" ) String linkType );

  @GremlinGroovy( "it.out" )
  Iterable<Concept> getAllOutNodes( );

  @GremlinGroovy( "it.in" )
  Iterable<Concept> getAllInNodes( );

  @JavaHandler
  Object getProperty( final String propertyName );

  @JavaHandler
  Set<String> getPropertyNames();

  abstract class Impl implements JavaHandlerContext<Vertex>, FramedMetaverseNode {

    public Object getProperty( final String propertyName ){
      return it().getProperty( propertyName );
    }

    public Set<String> getPropertyNames() {
      return it().getPropertyKeys();
    }
  }
}

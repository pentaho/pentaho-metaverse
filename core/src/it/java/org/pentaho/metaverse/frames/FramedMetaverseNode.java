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

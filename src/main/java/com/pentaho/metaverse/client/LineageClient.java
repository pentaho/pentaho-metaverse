/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */
package com.pentaho.metaverse.client;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.ILineageClient;
import com.pentaho.metaverse.graph.LineageGraphMap;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.metaverse.MetaverseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Created by mburgess on 1/21/15.
 */
public class LineageClient implements ILineageClient {

  protected static final int MAX_LOOPS = 10;

  /**
   * Generates a list of names of steps that create a field with the specified name
   *
   * @param transName      the name of the transformation
   * @param targetStepName the name of the step from which to start searching for the field
   * @param fieldNames     the name of the field(s) for which its creating step(s) will be returned
   * @return a list of names of steps that create a field with the specified name
   * @throws MetaverseException if an error occurred while searching for creator steps
   */
  public List<StepField> getCreatorSteps( String transName, String targetStepName, String... fieldNames )
    throws MetaverseException {

    if ( transName != null ) {
      for ( Object o : LineageGraphMap.getInstance().keySet() ) {
        if ( o instanceof TransMeta ) {
          TransMeta transMeta = (TransMeta) o;
          String transPath = transMeta.getFilename();
          if ( Const.isEmpty( transPath ) ) {
            transPath = transMeta.getPathAndName();
          }
          if ( transName.equals( transPath ) ) {
            return Collections.unmodifiableList( getCreatorSteps( transMeta, targetStepName, fieldNames ) );
          }
        }
      }
    }
    return new ArrayList<StepField>();
  }

  /**
   * Generates a list of names of steps that create a field with the specified name
   *
   * @param transMeta      a reference to the transformation metadata
   * @param targetStepName the name of the step from which to start searching for the field
   * @param fieldNames     the name of the field(s) for which its creating step(s) will be returned
   * @return a list of names of steps that create a field with the specified name
   * @throws MetaverseException if an error occurred while searching for creator steps
   */
  @Override
  public List<StepField> getCreatorSteps( TransMeta transMeta, String targetStepName, String... fieldNames )
    throws MetaverseException {
    List<StepField> stepNameList = new ArrayList<StepField>();
    try {
      Future<Graph> lineageGraphTask = LineageGraphMap.getInstance().get( transMeta );
      if ( lineageGraphTask != null ) {
        Graph lineageGraph = lineageGraphTask.get();

        // Call the internal creatorSteps() method, then get the resultant vertices out and into a list by name
        stepNameList = creatorSteps( lineageGraph, targetStepName, fieldNames );
      }
    } catch ( Exception e ) {
      throw new MetaverseException( e );
    }

    return Collections.unmodifiableList( stepNameList );
  }

  /**
   * Gets the origin steps and fields that contribute to the target field in the target step.
   * <p/>
   * The basic approach is to find the node(s) that create the target field, then see if that field is derived by
   * other(s). Then we get those Origin steps and so on
   *
   * @param transMeta      a reference to the transformation
   * @param targetStepName the name of the step from which to start searching for the field
   * @param fieldNames     the name of the field for which its origin step(s) will be returned
   * @return
   * @throws MetaverseException
   */
  @Override
  public Set<StepFieldTarget> getOriginSteps( TransMeta transMeta, String targetStepName, String... fieldNames )
    throws MetaverseException {

    Set<StepFieldTarget> originStepMap = new HashSet<StepFieldTarget>();

    try {
      Future<Graph> lineageGraphTask = LineageGraphMap.getInstance().get( transMeta );
      if ( lineageGraphTask != null ) {
        Graph lineageGraph = lineageGraphTask.get();
        // First, start with the fields that create the specified target field. Basically we want to find the Vertex
        // that has the given field name, but by traversing backwards along the steps' "hops_to" links. So start with
        // the specified (target) step, see if it creates the field, and if not, walk the "hops_to" links backwards
        // and in turn check those steps to see if they created the field.
        List<Vertex> creatorFields = creatorFields( lineageGraph, targetStepName, fieldNames );

        GremlinPipeline pipe = new GremlinPipeline( creatorFields )

          // Determine if some other field derives the creatorField. If so, we want to walk back along the "derives"
          // links in order to find a field that has no incoming "derives" links (meaning it wasn't derived from
          // some other field), then find the step that created it. It's possible that the current Vertex has no
          // "derives" links, which means the search is over.
          .ifThenElse(
            // Does any field derive this one?
            new PipeFunction<Vertex, Boolean>() {
              @Override
              public Boolean compute( Vertex it ) {
                return it.getVertices( Direction.IN, DictionaryConst.LINK_DERIVES ).iterator().hasNext();
              }
            },
            new PipeFunction<Vertex, GremlinPipeline>() {
              @Override
              public GremlinPipeline compute( Vertex it ) {
                // Do a lookahead in the loop function to see if there's a "derives" link. If not, emit the node;
                //  if so, don't emit the node. The loop will have followed that derives link separately, so it will be
                //  processed by the loop logic until it has no more derives links, at which point it will be emitted.
                return new GremlinPipeline( it ).in( DictionaryConst.LINK_DERIVES )
                  .loop( 1, new NumLoops( MAX_LOOPS ), new PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean>() {
                    @Override
                    public Boolean compute( LoopPipe.LoopBundle<Vertex> argument ) {
                      Vertex v = argument.getObject();
                      return ( v != null && !v.getEdges( Direction.IN, "derives" ).iterator().hasNext() );
                    }
                  } )
                  .transform( new FieldAndStepList() );
              }
            },
            new PipeFunction<Vertex, GremlinPipeline>() {
              @Override
              public GremlinPipeline compute( Vertex it ) {

                return new GremlinPipeline( it ).transform( new FieldAndStepList() );
              }
            } );

        List<List<String>> stepFieldList = (List<List<String>>) pipe.toList();
        if ( stepFieldList != null ) {
          for ( List<String> stepFieldEntry : stepFieldList ) {
            // Field is in first position,  step is in second
            StepFieldTarget newTarget = new StepFieldTarget( stepFieldEntry.get( 1 ), stepFieldEntry.get( 0 ), targetStepName );
            if ( !originStepMap.contains( newTarget ) ) {
              originStepMap.add( newTarget );
            }
          }
        }

      }
    } catch ( Exception e ) {
      throw new MetaverseException( e );
    }

    return originStepMap;
  }

  /**
   * Returns the paths between the origin field(s) and target field(s). A path in this context is an ordered list of
   * StepFieldOperations objects, each of which corresponds to a field at a certain step where operation(s) are
   * applied. The order of the list corresponds to the order of the steps from the origin step (see getOriginSteps())
   * to the target step. This method can be used to trace a target field back to its origin and discovering what
   * operations were performed upon it during it's lifetime. Inversely the path could be used to re-apply the operations
   * to the origin field, resulting in the field's "value" at each point in the path.
   *
   * @param transMeta      a reference to a transformation's metadata
   * @param targetStepName the target step name associated with the given field names
   * @param fieldNames     an array of field names associated with the target step, for which to find the step(s) and
   *                       field(s) and operation(s) that contributed to those fields
   * @return a map of target field name to an ordered list of StepFieldOperations objects, describing the path from the
   * origin step field to the target step field, including the operations performed.
   * @throws MetaverseException if an error occurred while finding the origin steps
   */
  public Map<String, List<StepFieldOperations>> getOperationPaths(
    TransMeta transMeta, String targetStepName, String... fieldNames ) throws MetaverseException {
    Map<String, List<StepFieldOperations>> operationPathMap = new HashMap<String, List<StepFieldOperations>>();

    return operationPathMap;
  }

  /**
   * This is an intermediate method that returns a pipeline which would determine the transformation steps that create
   * a field with the given name.
   *
   * @param fieldNames the target field name(s) for which to find the creating step(s)
   * @return a GremlinPipeline which will determine the steps that create the given field
   */
  protected List<StepField> creatorSteps(
    Graph lineageGraph, String targetStepName, String... fieldNames ) {

    List<StepField> creatorStepsList = new ArrayList<StepField>();

    // Call creatorFields, then follow the "creates" link back to the step nodes
    List<Vertex> creatorFieldNodes = creatorFields( lineageGraph, targetStepName, fieldNames );
    GremlinPipeline creatorStepsPipe =
      new GremlinPipeline( creatorFieldNodes ).as( "step" )
        .in( DictionaryConst.LINK_CREATES ).as( "field" )
        .select(
          new PipeFunction<Vertex, String>() {
            @Override
            public String compute( Vertex argument ) {
              return argument.getProperty( DictionaryConst.PROPERTY_NAME );
            }
          }
        );

    List<ArrayList<String>> stepFieldList = creatorStepsPipe.toList();
    for ( ArrayList<String> stepFields : stepFieldList ) {
      // Field is in first position,  step is in second
      creatorStepsList.add( new StepField( stepFields.get( 1 ), stepFields.get( 0 ) ) );
    }

    return creatorStepsList;
  }


  /**
   * This is an intermediate method that returns a pipeline which would determine the vertices with the given fieldname,
   * which were created by steps that have "hops to" links to anything on the front of the pipe. This method is not
   * meant to be run stand-alone, instead you normally have an existing pipe and add the pipeline returned by this
   * method.
   *
   * @param fieldNames the target fieldname for which to find the creating fields
   * @return a GremlinPipeline which will determine the creating fields for the given target field
   */
  protected List<Vertex> creatorFields( Graph lineageGraph, String targetStepName, String... fieldNames ) {

    List<Vertex> creatorFields = new ArrayList<Vertex>();
    Vertex stepNode = null;

    GremlinPipeline<Vertex, Vertex> stepNodePipe =
      new GremlinPipeline<Vertex, Vertex>( lineageGraph )
        .V( DictionaryConst.PROPERTY_NAME, targetStepName )
        .has( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP ).cast( Vertex.class );

    List<Vertex> stepNodeList = stepNodePipe.toList();
    if ( !Const.isEmpty( stepNodeList ) ) {
      // Steps have unique names in the graph, so this list should have one element, just grab the first one
      stepNode = stepNodeList.get( 0 );


      if ( fieldNames != null ) {
        for ( String targetFieldName : fieldNames ) {

          // Does the target step create this field?
          GremlinPipeline<Vertex, Vertex> testPipe =
            new GremlinPipeline<Vertex, Vertex>( stepNode )
              .out( DictionaryConst.LINK_CREATES )
              .has( DictionaryConst.PROPERTY_NAME, targetFieldName ).cast( Vertex.class );

          if ( testPipe.hasNext() ) {
            // We found the creator of the field, so add it to our results list
            creatorFields.add( testPipe.next() );
          } else {
            // Some previous step created this field, so walk the "hops to" links back from the target step, until you
            // find the step that creates the field
            GremlinPipeline<Vertex, Vertex> creatorPipe =
              new GremlinPipeline<Vertex, Vertex>( stepNode )
                .in( DictionaryConst.LINK_HOPSTO ).loop( 1,
                // {it.loops < MAX_LOOPS}
                new NumLoops<Vertex>( MAX_LOOPS ),
                // {it.object != null}
                new NoNullObjectsInLoop<Vertex>()
              )
                .out( DictionaryConst.LINK_CREATES )
                .has( DictionaryConst.PROPERTY_NAME, targetFieldName )
                .cast( Vertex.class );

            while ( creatorPipe.hasNext() ) {
              // These are the creator field nodes
              creatorFields.add( creatorPipe.next() );
            }
          }
        }
      }
    }
    return creatorFields;
  }

  protected List<Vertex> getVerticesWithNames( Graph g, String type, String... names ) {
    List<Vertex> result = new ArrayList<Vertex>( names == null ? 0 : names.length );
    if ( !Const.isEmpty( names ) && !Const.isEmpty( type ) ) {
      for ( String name : names ) {
        Iterable<Vertex> vertices = g.getVertices( DictionaryConst.PROPERTY_NAME, name );
        if ( vertices != null ) {
          for ( Vertex v : vertices ) {
            if ( type.equals( v.getProperty( DictionaryConst.PROPERTY_TYPE ) ) ) {
              result.add( v );
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * This is a loop closure that returns true if the current loop count is less than the given number, false otherwise
   *
   * @param <S> the type of object passed into the loop closure
   */
  protected static class NumLoops<S> implements PipeFunction<LoopPipe.LoopBundle<S>, Boolean> {
    private int numLoops = 1;

    public NumLoops( int numLoops ) {
      this.numLoops = numLoops;
    }

    @Override
    public Boolean compute( LoopPipe.LoopBundle argument ) {
      return argument.getLoops() < numLoops;
    }
  }

  /**
   * This is a simple loop emit closure that will emit the incoming object if its associated object is not null
   *
   * @param <S> the type of object passed into the loop emit closure
   */
  public static class NoNullObjectsInLoop<S> implements PipeFunction<LoopPipe.LoopBundle<S>, Boolean> {
    @Override
    public Boolean compute( LoopPipe.LoopBundle<S> argument ) {
      return argument.getObject() != null;
    }
  }

  /**
   * This class provides a transformation closure that takes a field vertex and creates a list containing two elements:
   * 1) the name of the field
   * 2) the name of the step that created the field
   */
  protected static class FieldAndStepList implements PipeFunction<Vertex, List<String>> {
    @Override
    public List<String> compute( Vertex it ) {
      return Arrays.asList(
        it.getProperty( DictionaryConst.PROPERTY_NAME ).toString(),
        it.getVertices( Direction.IN, DictionaryConst.LINK_CREATES )
          .iterator().next().getProperty( DictionaryConst.PROPERTY_NAME ).toString() );
    }
  }
}

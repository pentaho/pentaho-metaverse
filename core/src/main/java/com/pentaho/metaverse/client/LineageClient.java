/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ILineageClient;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.StepFieldOperations;
import org.pentaho.metaverse.api.model.Operations;
import com.pentaho.metaverse.graph.LineageGraphMap;
import com.pentaho.metaverse.util.MetaverseUtil;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.TransMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * LineageClient is a collection of methods that provide specific data- and metadata-lineage information, such as which
 * transformation steps have fields that contribute to other fields, what operations have been performed on fields in
 * a transformation, etc.
 */
public class LineageClient implements ILineageClient {

  protected static final int MAX_LOOPS = 50;

  private static final StepFieldOperationsPipeFunction STEPFIELDOPS_PIPE_FUNC = new StepFieldOperationsPipeFunction();

  /**
   * Finds the step(s) in the given transformation that created the given field, with respect to the given target step.
   * This means if a field has been renamed or derived from another field from another step, then the lineage graph
   * is traversed back from the target step to determine which steps contributed to the field in the target step.
   * This differs from getCreatorSteps() as the lineage graph traversal will not stop with a "creates" relationship;
   * rather, this method will traverse other relationships ("uses", "derives", e.g.) to find the actual origin fields
   * that comprise the final field in the target step.
   *
   * @param transMeta      a reference to a transformation's metadata
   * @param targetStepName the target step name associated with the given field names
   * @param fieldNames     a collection of field names associated with the target step, for which to find the step(s)
   *                       and field(s) that contributed to those fields
   * @return a map from target field name to step-field objects, where each step has created a field with
   * the returned name, and that field has contributed in some way to the specified target field.
   * @throws MetaverseException if an error occurred while finding the origin steps
   */
  @Override
  public Map<String, Set<StepField>> getOriginSteps( TransMeta transMeta, String targetStepName,
                                                     Collection<String> fieldNames ) throws MetaverseException {
    Map<String, Set<StepField>> originStepsMap = new HashMap<>();

    try {
      Future<Graph> lineageGraphTask = LineageGraphMap.getInstance().get( transMeta );
      if ( lineageGraphTask != null ) {
        Graph lineageGraph = lineageGraphTask.get();
        List<Vertex> targetFields = getTargetFields( lineageGraph, targetStepName, fieldNames );

        GremlinPipeline pipe = getOriginStepsPipe( targetFields );
        List<List<Vertex>> pathList = pipe.toList();
        if ( pathList != null ) {

          for ( List<Vertex> path : pathList ) {
            // Transform each path of vertices into a "path" of StepFieldOperations objects (basically save off
            // properties of each vertex into a new list)
            String targetField = path.get( 0 ).getProperty( DictionaryConst.PROPERTY_NAME );
            Set<StepField> pathSet = originStepsMap.get( targetField );

            if ( pathSet == null ) {
              pathSet = new HashSet<>();
              originStepsMap.put( targetField, pathSet );
            }

            Vertex v = path.get( path.size() - 1 );
            Map<String, String> stepField = STEPFIELDOPS_PIPE_FUNC.compute( v );
            String stepName = stepField.get( "stepName" );
            String fieldName = stepField.get( "fieldName" );

            pathSet.add( new StepField( stepName, fieldName ) );
          }
        }
      }
    } catch ( Exception e ) {
      throw new MetaverseException( e );
    }

    return originStepsMap;
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
  @Override
  public Map<String, Set<List<StepFieldOperations>>> getOperationPaths(
    TransMeta transMeta, String targetStepName, final Collection<String> fieldNames ) throws MetaverseException {

    Map<String, Set<List<StepFieldOperations>>> operationPathMap = new HashMap<>();

    try {
      Future<Graph> lineageGraphTask = LineageGraphMap.getInstance().get( transMeta );
      if ( lineageGraphTask != null ) {
        Graph lineageGraph = lineageGraphTask.get();

        if ( lineageGraph != null ) {

          // Get the creator field nodes for all the field names passed in
          List<Vertex> getTargetFields = getTargetFields( lineageGraph, targetStepName, fieldNames );


          // The "origin steps pipe" with a second param of true returns a pipeline that will return paths between
          // the origin field nodes and the target field node.
          GremlinPipeline pipe = getOriginStepsPipe( getTargetFields );
          List<List<Vertex>> pathList = pipe.toList();
          if ( pathList != null ) {

            for ( List<Vertex> path : pathList ) {
              // Transform each path of vertices into a "path" of StepFieldOperations objects (basically save off
              // properties of each vertex into a new list)
              List<StepFieldOperations> stepFieldOps = new ArrayList<>();
              String targetField = path.get( 0 ).getProperty( DictionaryConst.PROPERTY_NAME );
              Set<List<StepFieldOperations>> pathSet = operationPathMap.get( targetField );

              if ( pathSet == null ) {
                pathSet = new HashSet<>();
                operationPathMap.put( targetField, pathSet );
              }
              for ( Vertex v : path ) {
                Map<String, String> stepField = STEPFIELDOPS_PIPE_FUNC.compute( v );
                String stepName = stepField.get( "stepName" );
                String fieldName = stepField.get( "fieldName" );
                Operations operations = MetaverseUtil.convertOperationsStringToMap(
                  (String) v.getProperty( DictionaryConst.PROPERTY_OPERATIONS ) );

                stepFieldOps.add( 0, new StepFieldOperations( stepName, fieldName, operations ) );
              }
              pathSet.add( stepFieldOps );
            }
          }
        }
      }
    } catch ( Exception e ) {
      throw new MetaverseException( e );
    }

    return operationPathMap;
  }

  /**
   * This is an intermediate method that returns a pipeline which would determine the vertices with the given fieldname,
   * which were created by steps that have "hops to" links to anything on the front of the pipe. This method is not
   * meant to be run stand-alone, instead you normally have an existing pipe and add the pipeline returned by this
   * method.
   *
   * @param fieldNames the target fieldname for which to find the creating fields
   * @return a map which will determine the creating fields for the given target field
   */
  protected List<Vertex> getTargetFields(
    Graph lineageGraph, final String targetStepName, final Collection<String> fieldNames ) {

    // Find the target nodes (the field nodes output by the given target step)
    GremlinPipeline<Graph, Vertex> targetFieldNodesPipe =
      new GremlinPipeline<Graph, Vertex>( lineageGraph )
        // Get target step node
        .V( DictionaryConst.PROPERTY_NAME, targetStepName )
        .has( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP )
          // Get output field nodes
        .out( DictionaryConst.LINK_OUTPUTS )
        .has( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_FIELD ).cast( Vertex.class )
        // Only select the ones we want
        .filter(
          new PipeFunction<Vertex, Boolean>() {
            @Override
            public Boolean compute( Vertex v ) {
              Object name = v.getProperty( DictionaryConst.PROPERTY_NAME );
              return name != null && fieldNames.contains( name.toString() );
            }
          }
        )
        .cast( Vertex.class );

    return targetFieldNodesPipe.toList();
  }

  protected GremlinPipeline getOriginStepsPipe( List<Vertex> inV ) {
    GremlinPipeline pipe = new GremlinPipeline( inV )

      // Determine if some other field derives the creatorField. If so, we want to walk back along the "derives"
      // links in order to find a field that has no incoming "derives" links (meaning it wasn't derived from
      // some other field), then find the step that created it. It's possible that the current Vertex has no
      // "derives" links, which means the search is over.
      .ifThenElse(
        // Does any field derive this one?
        new HasDerivesOrJoinsLink(),
        new PipeFunction<Vertex, GremlinPipeline>() {
          @Override
          public GremlinPipeline compute( Vertex it ) {
            // Do a lookahead in the loop function to see if there's a "derives" link. If not, emit the node;
            //  if so, don't emit the node. The loop will have followed that derives link separately, so it will be
            //  processed by the loop logic until it has no more derives links, at which point it will be emitted.
            GremlinPipeline basePipe = new GremlinPipeline( it )
              .in( DictionaryConst.LINK_DERIVES, DictionaryConst.LINK_JOINS )
              .loop( 1, new NumLoops( MAX_LOOPS ), new NotNullAndNotDerivativeLoop() )
              .dedup();
            return basePipe.path();
          }
        },
        new PipeFunction<Vertex, GremlinPipeline>() {
          @Override
          public GremlinPipeline compute( Vertex it ) {
            return new GremlinPipeline( it ).path();
          }
        } );

    return pipe;
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
   * This loop emit closure checks for non-null vertices and whether the vertex has an in-link of "derives". It emits
   * vertices that do not have incoming "derives" links, and is used to prune the paths returned during methods like
   * getOriginSteps.
   */
  protected static class NotNullAndNotDerivativeLoop implements PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> {
    @Override
    public Boolean compute( LoopPipe.LoopBundle<Vertex> argument ) {
      Vertex v = argument.getObject();
      return ( v != null
        && !v.getVertices( Direction.IN, DictionaryConst.LINK_DERIVES ).iterator().hasNext() );
    }
  }

  /**
   * This pipe function determines whether the given vertex has an in-link of "derives", meaning that some other
   * field(s) have contributed to the value and/or metadata of this field
   */
  protected static class HasDerivesOrJoinsLink implements PipeFunction<Vertex, Boolean> {
    @Override
    public Boolean compute( Vertex v ) {
      return ( v != null && v.getVertices(
        Direction.IN, DictionaryConst.LINK_DERIVES, DictionaryConst.LINK_JOINS ).iterator().hasNext() );
    }
  }

  /**
   * This class provides a transformation closure that takes a field vertex and creates a list containing two elements:
   * 1) the name of the field
   * 2) the name of the step that created the field
   */
  protected static class StepFieldPipeFunction implements PipeFunction<Vertex, List<String>> {
    @Override
    public List<String> compute( Vertex it ) {
      return Arrays.asList(
        it.getProperty( DictionaryConst.PROPERTY_NAME ).toString(),
        it.getVertices( Direction.IN, DictionaryConst.LINK_OUTPUTS )
          .iterator().next().getProperty( DictionaryConst.PROPERTY_NAME ).toString() );
    }
  }

  /**
   * This class provides a transformation closure that takes a field vertex and creates a list containing two elements:
   * 1) the name of the field
   * 2) the name of the step that created the field
   */
  protected static class StepFieldOperationsPipeFunction implements PipeFunction<Vertex, Map<String, String>> {
    @Override
    public Map<String, String> compute( Vertex it ) {
      Map<String, String> stepFieldOpsMap = new HashMap<>();

      stepFieldOpsMap.put(
        "stepName",
        (String) it.getVertices( Direction.IN, DictionaryConst.LINK_OUTPUTS )
          .iterator().next().getProperty( DictionaryConst.PROPERTY_NAME )
      );

      stepFieldOpsMap.put(
        "fieldName",
        (String) it.getProperty( DictionaryConst.PROPERTY_NAME )
      );

      // Add operations if there are any, otherwise don't set the property
      String operations = it.getProperty( DictionaryConst.PROPERTY_OPERATIONS );
      if ( !Const.isEmpty( operations ) ) {
        stepFieldOpsMap.put( DictionaryConst.PROPERTY_METADATA_OPERATIONS, operations );
      }

      return stepFieldOpsMap;
    }
  }
            }

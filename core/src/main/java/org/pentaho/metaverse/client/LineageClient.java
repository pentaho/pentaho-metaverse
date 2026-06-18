/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.client;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.service.ServiceProviderInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ILineageClient;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.StepFieldOperations;
import org.pentaho.metaverse.api.model.Operations;
import org.pentaho.metaverse.graph.LineageGraphMap;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.pentaho.di.core.service.ServiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * LineageClient is a collection of methods that provide specific data- and metadata-lineage information, such as which
 * transformation steps have fields that contribute to other fields, what operations have been performed on fields in
 * a transformation, etc.
 */
@ServiceProvider( id = "LineageClient", description = "Provides specific data and metadata-lineage information", provides = ILineageClient.class )
public class LineageClient implements ILineageClient, ServiceProviderInterface<ILineageClient> {

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

        List<List<Vertex>> pathList = getOriginStepPaths( targetFields );
        if ( pathList != null ) {

          for ( List<Vertex> path : pathList ) {
            // Transform each path of vertices into a "path" of StepFieldOperations objects (basically save off
            // properties of each vertex into a new list)
            String targetField = path.get( 0 ).property( DictionaryConst.PROPERTY_NAME ).isPresent()
              ? path.get( 0 ).<String>value( DictionaryConst.PROPERTY_NAME ) : null;
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


          // The "origin steps paths" returns paths between the origin field nodes and the target field node.
          List<List<Vertex>> pathList = getOriginStepPaths( getTargetFields );
          if ( pathList != null ) {

            for ( List<Vertex> path : pathList ) {
              // Transform each path of vertices into a "path" of StepFieldOperations objects (basically save off
              // properties of each vertex into a new list)
              List<StepFieldOperations> stepFieldOps = new ArrayList<>();
              String targetField = path.get( 0 ).property( DictionaryConst.PROPERTY_NAME ).isPresent()
                ? path.get( 0 ).<String>value( DictionaryConst.PROPERTY_NAME ) : null;
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
                  v.property( DictionaryConst.PROPERTY_OPERATIONS ).isPresent()
                    ? v.<String>value( DictionaryConst.PROPERTY_OPERATIONS ) : null );

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
   * Returns the target field vertices in the lineage graph for the given target step and field names.
   */
  protected List<Vertex> getTargetFields(
    Graph lineageGraph, final String targetStepName, final Collection<String> fieldNames ) {

    return lineageGraph.traversal().V()
      .has( DictionaryConst.PROPERTY_NAME, targetStepName )
      .has( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP )
      .out( DictionaryConst.LINK_OUTPUTS )
      .has( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_FIELD )
      .filter( v -> {
        Object name = v.get().property( DictionaryConst.PROPERTY_NAME ).isPresent()
          ? v.get().value( DictionaryConst.PROPERTY_NAME ) : null;
        return name != null && fieldNames.contains( name.toString() );
      } )
      .toList();
  }

  /**
   * Returns all origin-to-target paths for the given list of target field vertices. Each returned list is a path
   * starting from the target field vertex and ending at the origin field vertex (no more in-derives/joins edges).
   */
  protected List<List<Vertex>> getOriginStepPaths( List<Vertex> inV ) {
    List<List<Vertex>> allPaths = new ArrayList<>();
    for ( Vertex startVertex : inV ) {
      List<Vertex> startPath = new ArrayList<>();
      startPath.add( startVertex );
      collectOriginPaths( startVertex, startPath, allPaths, new HashSet<>(), 0 );
    }
    return allPaths;
  }

  private void collectOriginPaths( Vertex current, List<Vertex> currentPath, List<List<Vertex>> allPaths,
                                    Set<Object> seen, int depth ) {
    if ( depth >= MAX_LOOPS ) {
      return;
    }
    Iterator<Vertex> inDerivesOrJoins = current.vertices( Direction.IN,
      DictionaryConst.LINK_DERIVES, DictionaryConst.LINK_JOINS );
    boolean hasDerivesOrJoins = inDerivesOrJoins.hasNext();

    if ( !hasDerivesOrJoins ) {
      allPaths.add( new ArrayList<>( currentPath ) );
    } else {
      Set<Object> newSeen = new HashSet<>( seen );
      newSeen.add( current.id() );
      inDerivesOrJoins = current.vertices( Direction.IN, DictionaryConst.LINK_DERIVES, DictionaryConst.LINK_JOINS );
      while ( inDerivesOrJoins.hasNext() ) {
        Vertex next = inDerivesOrJoins.next();
        if ( !newSeen.contains( next.id() ) ) {
          List<Vertex> newPath = new ArrayList<>( currentPath );
          newPath.add( next );
          collectOriginPaths( next, newPath, allPaths, newSeen, depth + 1 );
        }
      }
    }
  }

  /**
   * Computes a map containing step name and field name for the given field vertex.
   */
  protected static class StepFieldOperationsPipeFunction {
    public Map<String, String> compute( Vertex it ) {
      Map<String, String> stepFieldOpsMap = new HashMap<>();

      Iterator<Vertex> stepVertices = it.vertices( Direction.IN, DictionaryConst.LINK_OUTPUTS );
      if ( stepVertices.hasNext() ) {
        Vertex stepVertex = stepVertices.next();
        stepFieldOpsMap.put( "stepName",
          stepVertex.property( DictionaryConst.PROPERTY_NAME ).isPresent()
            ? stepVertex.<String>value( DictionaryConst.PROPERTY_NAME ) : null );
      }

      stepFieldOpsMap.put( "fieldName",
        it.property( DictionaryConst.PROPERTY_NAME ).isPresent()
          ? it.<String>value( DictionaryConst.PROPERTY_NAME ) : null );

      String operations = it.property( DictionaryConst.PROPERTY_OPERATIONS ).isPresent()
        ? it.<String>value( DictionaryConst.PROPERTY_OPERATIONS ) : null;
      if ( !Const.isEmpty( operations ) ) {
        stepFieldOpsMap.put( DictionaryConst.PROPERTY_METADATA_OPERATIONS, operations );
      }

      return stepFieldOpsMap;
    }
  }
}

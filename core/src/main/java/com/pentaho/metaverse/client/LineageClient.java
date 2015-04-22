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
import com.pentaho.metaverse.api.StepField;
import com.pentaho.metaverse.api.StepFieldOperations;
import com.pentaho.metaverse.api.model.Operations;
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
import com.pentaho.metaverse.api.MetaverseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

  protected static final int MAX_LOOPS = 20;

  private static final StepFieldPipeFunction STEPFIELD_PIPE_FUNC = new StepFieldPipeFunction();
  private static final StepFieldOperationsPipeFunction STEPFIELDOPS_PIPE_FUNC = new StepFieldOperationsPipeFunction();

  /**
   * Generates a list of names of steps that create a field with the specified name
   *
   * @param transName      the name of the transformation
   * @param targetStepName the name of the step from which to start searching for the field
   * @param fieldNames     the name of the field(s) for which its creating step(s) will be returned
   * @return a list of names of steps that create a field with the specified name
   * @throws MetaverseException if an error occurred while searching for creator steps
   */
  public Map<String, Set<StepField>> getCreatorSteps(
    String transName, String targetStepName, Collection<String> fieldNames ) throws MetaverseException {

    if ( transName != null ) {
      for ( Object o : LineageGraphMap.getInstance().keySet() ) {
        if ( o instanceof TransMeta ) {
          TransMeta transMeta = (TransMeta) o;
          String transPath = transMeta.getFilename();
          if ( Const.isEmpty( transPath ) ) {
            transPath = transMeta.getPathAndName();
          }
          if ( transName.equals( transPath ) ) {
            return Collections.unmodifiableMap( getCreatorSteps( transMeta, targetStepName, fieldNames ) );
          }
        }
      }
    }
    return new HashMap<String, Set<StepField>>();
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
  public Map<String, Set<StepField>> getCreatorSteps(
    TransMeta transMeta, String targetStepName, Collection<String> fieldNames ) throws MetaverseException {
    Map<String, Set<StepField>> creatorStepsMap = new HashMap<String, Set<StepField>>();
    try {
      Future<Graph> lineageGraphTask = LineageGraphMap.getInstance().get( transMeta );
      if ( lineageGraphTask != null ) {
        Graph lineageGraph = lineageGraphTask.get();

        // Call the internal creatorSteps() method, then get the resultant vertices out and into a list by name
        creatorStepsMap = creatorSteps( lineageGraph, targetStepName, fieldNames );
      }
    } catch ( Exception e ) {
      throw new MetaverseException( e );
    }

    return Collections.unmodifiableMap( creatorStepsMap );
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
  public Map<String, Set<StepField>> getOriginSteps(
    TransMeta transMeta, String targetStepName, Collection<String> fieldNames ) throws MetaverseException {

    Map<String, Set<StepField>> originStepsMap = new HashMap<String, Set<StepField>>();

    try {
      Future<Graph> lineageGraphTask = LineageGraphMap.getInstance().get( transMeta );
      if ( lineageGraphTask != null ) {
        Graph lineageGraph = lineageGraphTask.get();
        // First, start with the fields that create the specified target field. Basically we want to find the Vertex
        // that has the given field name, but by traversing backwards along the steps' "hops_to" links. So start with
        // the specified (target) step, see if it creates the field, and if not, walk the "hops_to" links backwards
        // and in turn check those steps to see if they created the field.
        Map<String, Set<Vertex>> creatorFields = creatorFields( lineageGraph, targetStepName, fieldNames );

        for ( String targetField : creatorFields.keySet() ) {

          GremlinPipeline pipe =
            getOriginStepsPipe( creatorFields.get( targetField ), false ).transform( new StepFieldPipeFunction() );
          List<List<String>> stepFieldList = (List<List<String>>) pipe.toList();
          if ( stepFieldList != null ) {
            Set<StepField> originStepsSet = originStepsMap.get( targetField );
            if ( originStepsSet == null ) {
              originStepsSet = new HashSet<StepField>();
              originStepsMap.put( targetField, originStepsSet );
            }
            for ( List<String> stepFieldEntry : stepFieldList ) {
              // Field is in first position,  step is in second
              StepField newStepField =
                new StepField( stepFieldEntry.get( 1 ), stepFieldEntry.get( 0 ) );
              originStepsSet.add( newStepField );
            }
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
    TransMeta transMeta, String targetStepName, Collection<String> fieldNames ) throws MetaverseException {

    Map<String, Set<List<StepFieldOperations>>> operationPathMap =
      new HashMap<String, Set<List<StepFieldOperations>>>();

    try {
      Future<Graph> lineageGraphTask = LineageGraphMap.getInstance().get( transMeta );
      if ( lineageGraphTask != null ) {
        Graph lineageGraph = lineageGraphTask.get();

        if ( lineageGraph != null ) {
          // Get the creator field nodes for all the field names passed in
          Map<String, Set<Vertex>> creatorFields = creatorFields( lineageGraph, targetStepName, fieldNames );

          for ( String targetField : creatorFields.keySet() ) {
            // The "origin steps pipe" with a second param of true returns a pipeline that will return paths between
            // the origin field nodes and the target field node.
            GremlinPipeline pipe = getOriginStepsPipe( creatorFields.get( targetField ), true );
            List<List<Vertex>> pathList = pipe.toList();
            if ( pathList != null ) {
              Set<List<StepFieldOperations>> pathSet = operationPathMap.get( targetField );
              if ( pathSet == null ) {
                pathSet = new HashSet<List<StepFieldOperations>>();
                operationPathMap.put( targetField, pathSet );
              }
              for ( List<Vertex> path : pathList ) {
                // Transform each path of vertices into a "path" of StepFieldOperations objects (basically save off
                // properties of each vertex into a new list)
                List<StepFieldOperations> stepFieldOps = new ArrayList<StepFieldOperations>();
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
      }
    } catch ( Exception e ) {
      throw new MetaverseException( e );
    }
    return operationPathMap;
  }

  /**
   * This is an intermediate method that returns a pipeline which would determine the transformation steps that create
   * a field with the given name.
   *
   * @param fieldNames the target field name(s) for which to find the creating step(s)
   * @return a GremlinPipeline which will determine the steps that create the given field
   */
  protected Map<String, Set<StepField>> creatorSteps(
    Graph lineageGraph, String targetStepName, Collection<String> fieldNames ) {

    Map<String, Set<StepField>> creatorStepsMap = new HashMap<String, Set<StepField>>();

    // Call creatorFields, then follow the "creates" link back to the step nodes
    Map<String, Set<Vertex>> creatorFieldNodes = creatorFields( lineageGraph, targetStepName, fieldNames );
    for ( String targetField : creatorFieldNodes.keySet() ) {

      GremlinPipeline creatorStepsPipe =
        new GremlinPipeline( creatorFieldNodes.get( targetField ) ).as( "step" )
          .in( DictionaryConst.LINK_CREATES ).as( "field" )
          .select(
            new PipeFunction<Vertex, String>() {
              @Override
              public String compute( Vertex argument ) {
                return argument.getProperty( DictionaryConst.PROPERTY_NAME );
              }
            }
          );

      Set<StepField> creatorStepsSet = creatorStepsMap.get( targetField );
      if ( creatorStepsSet == null ) {
        creatorStepsSet = new HashSet<StepField>();
        creatorStepsMap.put( targetField, creatorStepsSet );
      }

      List<ArrayList<String>> stepFieldList = creatorStepsPipe.toList();
      for ( ArrayList<String> stepFields : stepFieldList ) {
        // Field is in first position,  step is in second
        creatorStepsSet.add( new StepField( stepFields.get( 1 ), stepFields.get( 0 ) ) );
      }
    }

    return creatorStepsMap;
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
  protected Map<String, Set<Vertex>> creatorFields(
    Graph lineageGraph, final String targetStepName, Collection<String> fieldNames ) {

    final Map<String, Set<Vertex>> creatorFieldsMap = new HashMap<String, Set<Vertex>>();

    if ( fieldNames != null ) {
      // Go through each target field name individually. We use a map so as not to confuse which creator fields are
      // associated with which target fields
      // TODO maybe ConcurrentSkipListSet?
      for ( final String targetFieldName : fieldNames ) {
        // Need a final reference so we can access it from the pipeline
        final Set<Vertex> creatorFields = new HashSet<Vertex>();
        creatorFieldsMap.put( targetFieldName, creatorFields );

        // Get all the nodes with the same name as the target field. We will prune these away using rules
        GremlinPipeline<Vertex, Vertex> creatorPipe =
          new GremlinPipeline<Vertex, Vertex>( lineageGraph )
            .V( DictionaryConst.PROPERTY_NAME, targetFieldName )
            .has( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_FIELD )
            .as( "x" ).cast( Vertex.class )

            // Apply the pruning rules
            .filter(
              new PipeFunction<Vertex, Boolean>() {

                @Override
                public Boolean compute( Vertex v ) {

                  GremlinPipeline<Vertex, Vertex> deletesPipe =
                    new GremlinPipeline<Vertex, Vertex>( v ).in( DictionaryConst.LINK_DELETES );

                  // If the field is not deleted, send it along the pipeline to see if its creator step reaches the
                  // target step
                  if ( !deletesPipe.hasNext() ) {
                    return true;
                  }

                  // The field has been deleted. If it was deleted by the target step, then it can't be the one we're
                  // looking for
                  if ( deletesPipe.has( DictionaryConst.PROPERTY_NAME, targetFieldName ).hasNext() ) {
                    return false;
                  }

                  // We have to regenerate the deletesPipe, the "has" pipe will mutate it :(
                  deletesPipe = new GremlinPipeline<Vertex, Vertex>( v ).in( DictionaryConst.LINK_DELETES );

                  // The field wasn't deleted by the target step. Let's make sure it wasn't deleted BEFORE the target
                  // step. If it was, this isn't the vertex we're looking for :)  If it wasn't, then it must've been
                  // deleted AFTER the target step, which we don't care about, so let it go through the pipeline.
                  if ( !deletesPipe.in( DictionaryConst.LINK_HOPSTO )
                    .loop( 1, new NumLoops<Vertex>( MAX_LOOPS ), new NotNullAndNameMatches( targetStepName ) )
                    .hasNext() ) {
                    return false;
                  }

                  // None of the pruning rules apply, so let this vertex go through the pipeline
                  return true;
                }
              }
            ).back( "x" ).cast( Vertex.class ).as( "createdFields" )
            // Ok it gets even more confusing here. We're going to filter on only those nodes that are created by
            // the target step. We will add each of them to the creatorFields list. But then we need to go back
            // BEFORE the filter, and run all the nodes' creator steps through the "hops to" loop. We do that using
            // the "optional" pipe. We'll technically be processing the aforementioned nodes twice, but since the
            // loop will follow "hops to" links at least once, we will have "hopped past" the target step, and thus
            // those nodes will not be emitted at the end of the pipeline.
            .filter( new HasDirectLinkToNode( Direction.IN, DictionaryConst.LINK_CREATES, targetStepName ) )
            .sideEffect(
              new PipeFunction<Vertex, Vertex>() {
                @Override
                public Vertex compute( Vertex v ) {
                  // We've found one of the nodes we're looking for, so add it to the map
                  creatorFields.add( v );
                  return v;
                }
              }
            ).optional( "createdFields" )
            .in( DictionaryConst.LINK_CREATES )
            .out( DictionaryConst.LINK_HOPSTO )
            .loop( 1,
              new NumLoops<Vertex>( MAX_LOOPS ),
              // {it.object != null}
              new NoNullObjectsInLoop<Vertex>()
            )
            .has( DictionaryConst.PROPERTY_NAME, targetStepName ).back( "x" ).cast( Vertex.class );

        while ( creatorPipe.hasNext() ) {
          // These are the creator field nodes
          creatorFields.add( creatorPipe.next() );
        }
      }

    /*GremlinPipeline<Vertex, Vertex> stepNodePipe =
      new GremlinPipeline<Vertex, Vertex>( lineageGraph )
        .V( DictionaryConst.PROPERTY_NAME, targetStepName )
        .has( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP ).cast( Vertex.class );

    List<Vertex> stepNodeList = stepNodePipe.toList();
    if ( !Const.isEmpty( stepNodeList ) ) {
      // Steps have unique names in the graph, so this list should have one element, just grab the first one
      final Vertex stepNode = stepNodeList.get( 0 );

      if ( fieldNames != null ) {
        for ( String targetFieldName : fieldNames ) {
          Set<Vertex> creatorFields = creatorFieldsMap.get( targetFieldName );
          if ( creatorFields == null ) {
            creatorFields = new HashSet<Vertex>();
            creatorFieldsMap.put( targetFieldName, creatorFields );
          }

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
      }*/
    }
    return creatorFieldsMap;
  }


  protected GremlinPipeline getOriginStepsPipe( Set<Vertex> inV, final boolean returnPaths ) {
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
            if ( returnPaths ) {
              return basePipe.path();
            } else {
              return basePipe;
            }
          }
        },
        new PipeFunction<Vertex, GremlinPipeline>() {
          @Override
          public GremlinPipeline compute( Vertex it ) {
            GremlinPipeline basePipe = new GremlinPipeline( it );
            if ( returnPaths ) {
              return basePipe.path();
            } else {
              return basePipe;
            }
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
   * This is a simple loop emit closure that will emit the incoming object if its associated object is not null
   *
   * @param <S> the type of object passed into the loop emit closure
   */
  protected static class NoNullObjectsInLoop<S> implements PipeFunction<LoopPipe.LoopBundle<S>, Boolean> {
    @Override
    public Boolean compute( LoopPipe.LoopBundle<S> argument ) {
      return argument.getObject() != null;
    }
  }

  protected static class NotNullLoop implements PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> {

    @Override
    public Boolean compute( LoopPipe.LoopBundle<Vertex> argument ) {
      return argument != null && argument.getObject() != null;
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
   * This loop emit closure checks for non-null vertices whose name matches a specified value.
   */
  protected static class NotNullAndNameMatches extends NoNullObjectsInLoop<Vertex> {

    String nameToMatch = "";

    public NotNullAndNameMatches( String nameToMatch ) {
      super();
      this.nameToMatch = nameToMatch;
    }

    @Override
    public Boolean compute( LoopPipe.LoopBundle<Vertex> argument ) {
      if ( !super.compute( argument ) ) {
        return false;
      }
      Vertex v = argument.getObject();
      String vName = v.getProperty( DictionaryConst.PROPERTY_NAME );
      if ( vName == null ) {
        return ( nameToMatch == null );
      }
      return vName.equals( nameToMatch );
    }
  }

  /**
   * This pipe function determines whether the given vertex has an in-link of "derives", meaning that some other
   * field(s) have contributed to the value and/or metadata of this field
   */
  protected static class HasDerivesOrJoinsLink implements PipeFunction<Vertex, Boolean> {
    @Override
    public Boolean compute( Vertex v ) {
      return ( v != null
        && v.getVertices( Direction.IN, DictionaryConst.LINK_DERIVES, DictionaryConst.LINK_JOINS )
          .iterator().hasNext() );
    }
  }

  /**
   * This pipe function determines whether the given vertex has the desired name and type
   */
  protected static class HasNameAndType implements PipeFunction<Vertex, Boolean> {

    private String targetName;
    private String targetType;

    public HasNameAndType( String targetName, String targetType ) {
      this.targetName = targetName;
      this.targetType = targetType;
    }

    @Override
    public Boolean compute( Vertex v ) {
      return ( v != null )
        && v.getProperty( DictionaryConst.PROPERTY_TYPE ).equals( targetType )
        && v.getProperty( DictionaryConst.PROPERTY_NAME ).equals( targetName );
    }
  }

  /**
   * This pipe function determines whether the given vertex has an edge with the specified name and direction, and that
   * the vertex on the other end of the edge has the specified name
   */
  protected static class HasDirectLinkToNode implements PipeFunction<Vertex, Boolean> {

    Direction edgeDirection;
    String linkLabel;
    String linkedNodeName;

    public HasDirectLinkToNode( Direction edgeDirection, String linkLabel, String linkedNodeName ) {
      this.edgeDirection = edgeDirection;
      this.linkLabel = linkLabel;
      this.linkedNodeName = linkedNodeName;
    }

    @Override
    public Boolean compute( Vertex v ) {
      return ( v != null )
        && new GremlinPipeline<Vertex, Boolean>( v.getVertices( edgeDirection, linkLabel ) )
          .has( DictionaryConst.PROPERTY_NAME, linkedNodeName ).hasNext();
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
        it.getVertices( Direction.IN, DictionaryConst.LINK_CREATES )
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
      Map<String, String> stepFieldOpsMap = new HashMap<String, String>();

      stepFieldOpsMap.put(
        "stepName",
        (String) it.getVertices( Direction.IN, DictionaryConst.LINK_CREATES )
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

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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

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
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by mburgess on 1/21/15.
 */
public class LineageClient implements ILineageClient {

  private static final int MAX_LOOPS = 10;

  public List<String> getCreatorSteps( String transName, String targetStepName, String fieldName )
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
            return Collections.unmodifiableList( getCreatorSteps( transMeta, targetStepName, fieldName ) );
          }
        }
      }
    }
    return new ArrayList<String>();
  }

  @Override
  public List<String> getCreatorSteps( TransMeta transMeta, String targetStepName, String fieldName )
    throws MetaverseException {
    List<String> stepNameList = new ArrayList<String>();
    try {
      Future<Graph> lineageGraphTask = LineageGraphMap.getInstance().get( transMeta );
      if ( lineageGraphTask != null ) {
        Graph lineageGraph = lineageGraphTask.get();
        GremlinPipeline creatorStepsPipe =
          new GremlinPipeline( lineageGraph ).V( DictionaryConst.PROPERTY_NAME, targetStepName );
        creatorStepsPipe.add( creatorSteps( fieldName ) );
        List<Vertex> stepNodeList = creatorStepsPipe.toList();
        if ( !Const.isEmpty( stepNodeList ) ) {
          for ( Vertex stepNode : stepNodeList ) {
            stepNameList.add( stepNode.getProperty( DictionaryConst.PROPERTY_NAME ).toString() );
          }
        }

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
   * @param transMeta
   * @param targetStepName
   * @param fieldName
   * @return
   * @throws MetaverseException
   */
  @Override
  public Multimap<String, String> getOriginSteps( TransMeta transMeta, String targetStepName, String fieldName )
    throws MetaverseException {

    Multimap<String, String> originStepMap = HashMultimap.create();

    try {
      Future<Graph> lineageGraphTask = LineageGraphMap.getInstance().get( transMeta );
      if ( lineageGraphTask != null ) {
        Graph lineageGraph = lineageGraphTask.get();
        GremlinPipeline pipe = new GremlinPipeline( lineageGraph ).V( DictionaryConst.PROPERTY_NAME, targetStepName )
          .add( creatorFields( fieldName ) )
            //.ifThenElse{it.in('derives').hasNext()}{it.in('derives').loop(1){it.loops<10}{it.object != null}.transform{[it.name,it.in('creates').name.first()]}}{it.transform{[it.name,it.in('creates').name.first()]}}
          .ifThenElse(
            // it.in('derives').hasNext()
            new PipeFunction<Vertex, Boolean>() {
              @Override
              public Boolean compute( Vertex it ) {
                return it.getVertices( Direction.IN, DictionaryConst.LINK_DERIVES ).iterator().hasNext();
              }
            },
            new PipeFunction<Vertex, GremlinPipeline>() {
              @Override
              public GremlinPipeline compute( Vertex it ) {
                //it.in('derives').loop(1){it.loops<10}{it.object != null}.transform{[it.name,it.in('creates').name.first()]
                return new GremlinPipeline( it ).in( DictionaryConst.LINK_DERIVES )
                  .loop( 1, new NumLoops( MAX_LOOPS ), new NoNullObjectsInLoop() )
                  .transform( new FieldAndStepList() );
              }
            },
            new PipeFunction<Vertex, GremlinPipeline>() {
              @Override
              public GremlinPipeline compute( Vertex it ) {

                return new GremlinPipeline( it ).transform( new FieldAndStepList() );
              }
            }
          );

        List<List<String>> stepFieldList = (List<List<String>>) pipe.toList();
        if ( stepFieldList != null ) {
          for ( List<String> stepFieldEntry : stepFieldList ) {
            originStepMap.put( stepFieldEntry.get( 0 ), stepFieldEntry.get( 1 ) );
          }
        }

      }
    } catch ( Exception e ) {
      throw new MetaverseException( e );
    }

    return originStepMap;
  }

  /**
   * This is an intermediate method that returns a pipeline which would determine the transformation steps that create
   * a field with the given name.
   *
   * @param fieldName the target fieldname for which to find the creating step(s)
   * @return a GremlinPipeline which will determine the steps that create the given field
   */
  protected GremlinPipeline<Vertex, Vertex> creatorSteps( String fieldName ) {
    return creatorFields( fieldName ).back( 2 ).cast( Vertex.class );
  }


  /**
   * This is an intermediate method that returns a pipeline which would determine the vertices with the given fieldname,
   * which were created by steps that have "hops to" links to anything on the front of the pipe. This method is not
   * meant to be run stand-alone, instead you normally have an existing pipe and add the pipeline returned by this
   * method.
   *
   * @param fieldName the target fieldname for which to find the creating fields
   * @return a GremlinPipeline which will determine the creating fields for the given target field
   */
  protected GremlinPipeline<Vertex, Vertex> creatorFields( String fieldName ) {
    GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<Vertex, Vertex>();
    return pipe.in( DictionaryConst.LINK_HOPSTO ).loop( 1,
      // {it.loops < MAX_LOOPS}
      new NumLoops<Vertex>( MAX_LOOPS ),
      // {it.object != null}
      new NoNullObjectsInLoop<Vertex>()
    )
      .out( DictionaryConst.LINK_CREATES ).has( DictionaryConst.PROPERTY_NAME, fieldName ).cast( Vertex.class );
  }

  /**
   * This is a loop closure that returns true if the current loop count is less than the given number, false otherwise
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
   *  1) the name of the field
   *  2) the name of the step that created the field
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

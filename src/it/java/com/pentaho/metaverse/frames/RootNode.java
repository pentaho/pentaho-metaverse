/*
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

package com.pentaho.metaverse.frames;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;

/**
 * User: RFellows Date: 9/4/14
 */
public interface RootNode extends FramedMetaverseNode {
  @Property( "division" )
  public String getDivision();

  @Property( "project" )
  public String getProject();

  @GremlinGroovy( "it.out.loop(1){it.loops < 10}{it.object.type == 'Transformation'}.dedup" )
  public Iterable<TransformationNode> getTransformations();

  @GremlinGroovy( "it.out.loop(1){it.loops < 10}{it.object.type == 'Transformation' && it.object.name == name }.dedup" )
  public TransformationNode getTransformation( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.loop(1){it.loops < 10}{it.object.type == 'Job'}.dedup" )
  public Iterable<JobNode> getJobs();

  @GremlinGroovy( "it.out.loop(1){it.loops < 10}{it.object.type == 'Job' && it.object.name == name }.dedup" )
  public JobNode getJob( @GremlinParam( "name" ) String name );

  @Adjacency( label = "", direction = Direction.IN )
  public Iterable<FramedMetaverseNode> getEntities();

  @GremlinGroovy( "it.out.has( 'name', T.eq, name )" )
  public FramedMetaverseNode getEntity( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Select values'}.as('step').in('contains').has('name', T.eq, 'Populate Table From File').back('step')" )
  public SelectValuesTransStepNode getSelectValuesStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Sacramento crime stats 2006 file'}.as('step').in('contains').has('name', T.eq, 'Populate Table From File').back('step')" )
  public TextFileInputStepNode getTextFileInputStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Get Customers'}.as('step').in('contains').has('name', T.eq, 'Textfile input - filename from field').back('step')" )
  public TextFileInputStepNode getTextFileInputStepNode_filenameFromField();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Database Connection' }" )
  public Iterable<DatasourceNode> getDatasourceNodes();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Database Connection' && it.object.name == name }" )
  public DatasourceNode getDatasourceNode( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Demo table crime stats output'}.as('step').in('contains').has('name', T.eq, 'Populate Table From File').back('step')" )
  public TableOutputStepNode getTableOutputStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == name}.as('step').in('contains').has('name', T.eq, 'value_mapper').back('step')" )
  public ValueMapperStepNode getValueMapperStepNode( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == stepName}.as('step').in('contains').has('name', T.eq, transformationName).back('step')" )
  public TextFileOutputStepNode getTextFileOutputStepNode(
    @GremlinParam( "transformationName" ) String transformationName,
    @GremlinParam( "stepName" ) String stepName );

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Merge Join'}.as('step').in('contains').has('name', T.eq, 'merge_join').back('step')" )
  public MergeJoinStepNode getMergeJoinStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'CSV file input'}.as('step').in('contains').has('name', T.eq, 'CSV Input').back('step')" )
  public CsvFileInputStepNode getCsvFileInputStepNode();


}

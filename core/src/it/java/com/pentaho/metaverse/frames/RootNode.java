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

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'Transformation'}.out().dedup" )
  public Iterable<TransformationNode> getTransformations();

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'Transformation'}.out(){it.object.name == name }.dedup" )
  public TransformationNode getTransformation( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'Job'}.out().dedup" )
  public Iterable<JobNode> getJobs();

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'Job'}.out(){ it.object.name == name }.dedup" )
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

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'Database Connection'}.out()" )
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

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Merge Join').as('step').in('contains').has('name', T.eq, 'merge_join').back('step')" )
  public MergeJoinStepNode getMergeJoinStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Stream lookup'}.as('step').in('contains').has('name', T.eq, 'stream_lookup').back('step')" )
  public StreamLookupStepNode getStreamLookupStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Calculator'}.as('step').in('contains').has('name', T.eq, 'calculator').back('step')" )
  public CalculatorStepNode getCalculatorStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'CSV file input').as('step').in('contains').has('name', T.eq, 'CSV Input').back('step')" )
  public CsvFileInputStepNode getCsvFileInputStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Microsoft Excel Input'}.as('step').in('contains').has('name', T.eq, 'excel_input').back('step')" )
  public ExcelInputStepNode getExcelInputStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 10}{it.object.type == 'Transformation Step' && it.object.name == 'Microsoft Excel Input'}.as('step').in('contains').has('name', T.eq, 'Excel input - filename from field').back('step')" )
  public ExcelInputStepNode getExcelInputFileNameFromFieldStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Group by').as('step').in('contains').has('name', T.eq, 'group_by').back('step')" )
//  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Group by'}.as('step').in('contains').has('name', T.eq, 'group_by').back('step')" )
  public GroupByStepNode getGroupByStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'MongoDB Connection' }.out()" )
  public Iterable<MongoDbDatasourceNode> getMongoDbDatasourceNodes();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Microsoft Excel Output'}.as('step').in('contains').has('name', T.eq, 'excel_output').back('step')" )
  public ExcelOutputStepNode getExcelOutputStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == stepName}.as('step').in('contains').has('name', T.eq, 'splitFields').back('step')" )
  public SplitFieldsStepNode getSplitFieldsStepNodeByName( @GremlinParam( "stepName" ) String stepName );

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'String operations'}.as('step').in('contains').has('name', T.eq, 'string_operations').back('step')" )
  public StringOperationsStepNode getStringOperationsStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Strings cut'}.as('step').in('contains').has('name', T.eq, 'strings_cut').back('step')" )
  public StringsCutStepNode getStringsCutStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Replace in string'}.as('step').in('contains').has('name', T.eq, 'strings_replace').back('step')" )
  public StringsReplaceStepNode getStringsReplaceStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 2}{it.object.name == 'Transformation Step'}.out().has('name', T.eq, 'Transformation Executor').as('step').in('contains').has('name', T.eq, 'trans-executor-parent').back('step')" )
  public TransExecutorStepNode getTransExecutorStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 2}{it.object.name == 'Transformation Step'}.out().has('name', T.eq, 'Copy rows to result').as('step').in('contains').has('name', T.eq, 'trans-executor-child').back('step')" )
  public RowsToResultStepNode getRowsToResultStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Fixed file input'}.as('step').in('contains').has('name', T.eq, 'fixed_file_input').back('step')" )
  public FixedFileInputStepNode getFixedFileInputStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == name}.as('step').in('contains').has('name', T.eq, 'get_xml_data').back('step')" )
  public GetXMLDataStepNode getGetXMLDataStepNode( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == name}.as('step').in('contains').has('name', T.eq, 'filter_rows').back('step')" )
  public FilterRowsStepNode getFilterRowsStepNode( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'HTTP Client'}.as('step').in('contains').has('name', T.eq, 'HTTP_client').back('step')" )
  public HttpClientStepNode getHttpClientStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'HTTP Client'}.as('step').in('contains').has('name', T.eq, 'HTTP_client - url from field').back('step')" )
  public HttpClientStepNode getHttpClientStepNode_urlFromField();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'HTTP Post'}.as('step').in('contains').has('name', T.eq, 'HTTP_Post').back('step')" )
  public HttpPostStepNode getHttpPostStepNode();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'HTTP Post'}.as('step').in('contains').has('name', T.eq, 'HTTP_Post - url from field').back('step')" )
  public HttpPostStepNode getHttpPostStepNode_urlFromField();

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == stepName}.as('step').in('contains').has('name', T.eq, transformationName).back('step')" )
  public XMLOutputStepNode getXMLOutputStepNode(
    @GremlinParam( "transformationName" ) String transformationName,
    @GremlinParam( "stepName" ) String stepName );


//  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == stepName}.as('step').in('contains').has('name', T.eq, 'rest_client').back('step')" )
  @GremlinGroovy( "it.out.loop(1){it.loops < 10}{it.object.name == 'Transformation Step'}.out().has( 'name', T.eq, stepName).as('step').in('contains').has('name', T.eq, 'rest_client').back('step')" )
  public RestClientStepNode getRestClientStepNode( @GremlinParam( "stepName" ) String stepName );

}

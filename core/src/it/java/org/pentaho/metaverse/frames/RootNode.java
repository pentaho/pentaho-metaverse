/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;

/**
 * User: RFellows Date: 9/4/14
 */
public interface RootNode extends FramedMetaverseNode {
  @Property( "division" )
  String getDivision();

  @Property( "project" )
  String getProject();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out().dedup" )
  Iterable<TransformationNode> getTransformations();

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'Transformation'}.out(){it.object.name == name }.dedup" )
  TransformationNode getTransformation( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'Job'}.out().dedup" )
  Iterable<JobNode> getJobs();

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'Job'}.out(){ it.object.name == name }.dedup" )
  JobNode getJob( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq,  stepName).as('step').in('contains').has('name', T.eq, transformationName).back('step')" )
  TransformationStepNode getStepNode(
    @GremlinParam( "transformationName" ) String transformationName,
    @GremlinParam( "stepName" ) String stepName );

  @Adjacency( label = "", direction = Direction.IN )
  Iterable<FramedMetaverseNode> getEntities();

  @GremlinGroovy( "it.out.has( 'name', T.eq, name )" )
  FramedMetaverseNode getEntity( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Select values').as('step').in('contains').has('name', T.eq, 'Populate Table From File').back('step')" )
  SelectValuesTransStepNode getSelectValuesStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq,  stepName).as('step').in('contains').has('name', T.eq, transformationName).back('step')" )
  FileInputStepNode getFileInputStepNode(
    @GremlinParam( "transformationName" ) String transformationName,
    @GremlinParam( "stepName" ) String stepName );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Get Customers').as('step').in('contains').has('name', T.eq, 'Old Textfile input - filename from field').back('step')" )
  FileInputStepNode getOldTextFileInputStepNode_filenameFromField();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Get Customers').as('step').in('contains').has('name', T.eq, 'Textfile input - filename from field').back('step')" )
  FileInputStepNode getTextFileInputStepNode_filenameFromField();

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'Database Connection'}.out()" )
  Iterable<DatasourceNode> getDatasourceNodes();

  @GremlinGroovy( "it.out.has('name', T.eq, 'External Connection').out.has('name', T.eq, 'Database Connection').out.has('name', T.eq, name)" )
  DatasourceNode getDatasourceNode( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Demo table crime stats output').as('step').in('contains').has('name', T.eq, 'Populate Table From File').back('step')" )
  TableOutputStepNode getTableOutputStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Table input').as('step').in('contains').has('name', T.eq, 'merge_join').back('step')" )
  TableInputStepNode getTableInputStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq,  stepName).as('step').in('contains').has('name', T.eq, transformationName).back('step')" )
  TextFileOutputStepNode getTextFileOutputStepNode(
    @GremlinParam( "transformationName" ) String transformationName,
    @GremlinParam( "stepName" ) String stepName );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Merge Join').as('step').in('contains').has('name', T.eq, 'merge_join').back('step')" )
  MergeJoinStepNode getMergeJoinStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq,  'Stream lookup').as('step').in('contains').has('name', T.eq, 'stream_lookup').back('step')" )
  StreamLookupStepNode getStreamLookupStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Calculator').as('step').in('contains').has('name', T.eq, 'calculator').back('step')" )
  CalculatorStepNode getCalculatorStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'CSV file input').as('step').in('contains').has('name', T.eq, 'CSV Input').back('step')" )
  CsvFileInputStepNode getCsvFileInputStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Microsoft Excel Input').as('step').in('contains').has('name', T.eq, 'excel_input').back('step')" )
  ExcelInputStepNode getExcelInputStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Microsoft Excel Input').as('step').in('contains').has('name', T.eq, 'Excel input - filename from field').back('step')" )
  ExcelInputStepNode getExcelInputFileNameFromFieldStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Group by').as('step').in('contains').has('name', T.eq, 'group_by').back('step')" )
  GroupByStepNode getGroupByStepNode();

  @GremlinGroovy( "it.out.out.has('name', T.eq, 'MongoDB Connection').out()" )
  Iterable<MongoDbDatasourceNode> getMongoDbDatasourceNodes();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Microsoft Excel Output').as('step').in('contains').has('name', T.eq, 'excel_output').back('step')" )
  ExcelOutputStepNode getExcelOutputStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, stepName).as('step').in('contains').has('name', T.eq, 'splitFields').back('step')" )
  SplitFieldsStepNode getSplitFieldsStepNodeByName( @GremlinParam( "stepName" ) String stepName );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Transformation Executor').as('step').in('contains').has('name', T.eq, 'trans-executor-parent').back('step')" )
  TransExecutorStepNode getTransExecutorStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Copy rows to result').as('step').in('contains').has('name', T.eq, 'trans-executor-child').back('step')" )
  RowsToResultStepNode getRowsToResultStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'Fixed file input').as('step').in('contains').has('name', T.eq, 'fixed_file_input').back('step')" )
  FixedFileInputStepNode getFixedFileInputStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, name).as('step').in('contains').has('name', T.eq, 'get_xml_data').back('step')" )
  GetXMLDataStepNode getGetXMLDataStepNode( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == name}.as('step').in('contains').has('name', T.eq, 'filter_rows').back('step')" )
  FilterRowsStepNode getFilterRowsStepNode( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'HTTP Client').as('step').in('contains').has('name', T.eq, 'HTTP_client').back('step')" )
  HttpClientStepNode getHttpClientStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'HTTP Client').as('step').in('contains').has('name', T.eq, 'HTTP_client - url from field').back('step')" )
  HttpClientStepNode getHttpClientStepNode_urlFromField();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'HTTP Post').as('step').in('contains').has('name', T.eq, 'HTTP_Post').back('step')" )
  HttpPostStepNode getHttpPostStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'HTTP Post').as('step').in('contains').has('name', T.eq, 'HTTP_Post - url from field').back('step')" )
  HttpPostStepNode getHttpPostStepNode_urlFromField();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, stepName).as('step').in('contains').has('name', T.eq, transformationName).back('step')" )
  XMLOutputStepNode getXMLOutputStepNode(
    @GremlinParam( "transformationName" ) String transformationName,
    @GremlinParam( "stepName" ) String stepName );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, stepName).as('step').in('contains').has('name', T.eq, 'rest_client').back('step')" )
  RestClientStepNode getRestClientStepNode( @GremlinParam( "stepName" ) String stepName );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation Step').out.has('name', T.eq, 'MongoDB Input').as('step').in('contains').has('name', T.eq, 'mongo_input').back('step')" )
  MongoDbInputStepNode getMongoDbInputStepNode();
}

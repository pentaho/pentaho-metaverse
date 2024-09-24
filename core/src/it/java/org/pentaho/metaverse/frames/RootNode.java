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

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out('typeconcept').dedup" )
  Iterable<TransformationNode> getTransformations();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out('typeconcept').has('name', T.eq, name)" )
  Iterable<TransformationNode> getTransformations( @GremlinParam( "name" ) String name );

  /**
   * We only want to consider transformations connected to the "repo" node, transformation nodes not connected to the
   * repo node might represent sub-transformations.
   */
  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out('typeconcept').has('name', T.eq, name).as"
    + "('transformation').inE.hasNot('executes').back('transformation')" )
  TransformationNode getTransformation( @GremlinParam( "name" ) String name );

  /**
   * A transformation node that is NOT connected to the "repo" node.
   */
  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out('typeconcept').has('name', T.eq, name).as('transformation').in('executes').back('transformation')" )
  TransformationNode getSubTransformation( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'Job'}.out('typeconcept').dedup" )
  Iterable<JobNode> getJobs();

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'Job'}.out(){ it.object.name == name }.dedup" )
  JobNode getJob( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq,  stepName).as('step').in('contains').has('name', T.eq, transformationName).back('step')" )
  TransformationStepNode getStepNode(
    @GremlinParam( "transformationName" ) String transformationName,
    @GremlinParam( "stepName" ) String stepName );

  @Adjacency( label = "", direction = Direction.IN )
  Iterable<FramedMetaverseNode> getEntities();

  @GremlinGroovy( "it.out.has( 'name', T.eq, name )" )
  FramedMetaverseNode getEntity( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Select values').as('step').in('contains').has('name', T.eq, 'Populate Table From File').back('step')" )
  SelectValuesTransStepNode getSelectValuesStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq,  stepName).as('step').in('contains').has('name', T.eq, transformationName).back('step')" )
  FileInputStepNode getFileInputStepNode(
    @GremlinParam( "transformationName" ) String transformationName,
    @GremlinParam( "stepName" ) String stepName );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Get Customers').as('step').in('contains').has('name', T.eq, 'Old Textfile input - filename from field').back('step')" )
  FileInputStepNode getOldTextFileInputStepNode_filenameFromField();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Get Customers').as('step').in('contains').has('name', T.eq, 'Textfile input - filename from field').back('step')" )
  FileInputStepNode getTextFileInputStepNode_filenameFromField();

  @GremlinGroovy( "it.out.loop(1){it.loops < 5}{it.object.name == 'Database Connection'}.out()" )
  Iterable<DatasourceNode> getDatasourceNodes();

  @GremlinGroovy( "it.out.has('name', T.eq, 'External Connection').out.has('name', T.eq, 'Database Connection').out('typeconcept').has('name', T.eq, name)" )
  DatasourceNode getDatasourceNode( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Sacramento crime stats 2006 file').as('step').in('contains').has('name', T"
    + ".eq, 'Populate Table From File').back('step')" )
  TextFileInputNode getTextFileInputNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Demo table crime stats output [1]').as('step').in('contains').has('name', T"
    + ".eq, 'Populate Table From File').back('step')" )
  TableOutputStepNode getTableOutputStepNode1();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Demo table crime stats output [2]').as('step').in('contains').has('name', T"
    + ".eq, 'Populate Table From File').back('step')" )
  TableOutputStepNode getTableOutputStepNode2();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Table input').as('step').in('contains').has('name', T.eq, 'merge_join').back('step')" )
  TableInputStepNode getTableInputStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq,  stepName).as('step').in('contains').has('name', T.eq, transformationName).back('step')" )
  TextFileOutputStepNode getTextFileOutputStepNode(
    @GremlinParam( "transformationName" ) String transformationName,
    @GremlinParam( "stepName" ) String stepName );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Merge Join').as('step').in('contains').has('name', T.eq, 'merge_join').back('step')" )
  MergeJoinStepNode getMergeJoinStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq,  'Stream lookup').as('step').in('contains').has('name', T.eq, 'stream_lookup').back('step')" )
  StreamLookupStepNode getStreamLookupStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Calculator').as('step').in('contains').has('name', T.eq, 'calculator').back('step')" )
  CalculatorStepNode getCalculatorStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'CSV file input').as('step').in('contains').has('name', T.eq, 'CSV Input').back('step')" )
  CsvFileInputStepNode getCsvFileInputStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Group by').as('step').in('contains').has('name', T.eq, 'group_by').back('step')" )
  GroupByStepNode getGroupByStepNode();


  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, stepName).as('step').in('contains').has('name', T.eq, 'splitFields').back('step')" )
  SplitFieldsStepNode getSplitFieldsStepNodeByName( @GremlinParam( "stepName" ) String stepName );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Transformation Executor').as('step').in('contains').has('name', T.eq, 'trans-executor-parent').back('step')" )
  TransExecutorStepNode getTransExecutorStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Copy rows to result').as('step').in('contains').has('name', T.eq, 'trans-executor-child').back('step')" )
  RowsToResultStepNode getRowsToResultStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'Fixed file input').as('step').in('contains').has('name', T.eq, 'fixed_file_input').back('step')" )
  FixedFileInputStepNode getFixedFileInputStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, name).as('step').in('contains').has('name', T.eq, 'get_xml_data').back('step')" )
  GetXMLDataStepNode getGetXMLDataStepNode( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == name}.as('step').in('contains').has('name', T.eq, 'filter_rows').back('step')" )
  FilterRowsStepNode getFilterRowsStepNode( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'HTTP Client').as('step').in('contains').has('name', T.eq, 'HTTP_client').back('step')" )
  HttpClientStepNode getHttpClientStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'HTTP Client').as('step').in('contains').has('name', T.eq, 'HTTP_client - url from field').back('step')" )
  HttpClientStepNode getHttpClientStepNode_urlFromField();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'HTTP Post').as('step').in('contains').has('name', T.eq, 'HTTP_Post').back('step')" )
  HttpPostStepNode getHttpPostStepNode();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, 'HTTP Post').as('step').in('contains').has('name', T.eq, 'HTTP_Post - url from field').back('step')" )
  HttpPostStepNode getHttpPostStepNode_urlFromField();

  @GremlinGroovy( "it.out.has('name', T.eq, 'Transformation').out.has('name', T.eq, 'Transformation Step').out('typeconcept').has('name', T.eq, stepName).as('step').in('contains').has('name', T.eq, transformationName).back('step')" )
  XMLOutputStepNode getXMLOutputStepNode(
    @GremlinParam( "transformationName" ) String transformationName,
    @GremlinParam( "stepName" ) String stepName );

}

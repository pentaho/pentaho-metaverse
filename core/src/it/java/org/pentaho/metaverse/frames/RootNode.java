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


package org.pentaho.metaverse.frames;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.function.Function;

/**
 * User: RFellows Date: 9/4/14
 */
public class RootNode extends Concept {
  public RootNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public String getDivision() {
    return getStringValue( "division" );
  }

  public String getProject() {
    return getStringValue( "project" );
  }

  public List<TransformationNode> getTransformations() {
    List<Vertex> result = graph.traversal().V( vertex.id() )
      .out().has( "name", "Transformation" ).out( "typeconcept" ).dedup().toList();
    return wrapAs( result.iterator(), v -> new TransformationNode( v, graph ) );
  }

  public List<TransformationNode> getTransformations( String name ) {
    List<Vertex> result = graph.traversal().V( vertex.id() )
      .out().has( "name", "Transformation" ).out( "typeconcept" ).has( "name", name ).toList();
    return wrapAs( result.iterator(), v -> new TransformationNode( v, graph ) );
  }

  public TransformationNode getTransformation( String name ) {
    List<Vertex> result = graph.traversal().V( vertex.id() )
      .out().has( "name", "Transformation" ).out( "typeconcept" ).has( "name", name )
      .filter( tv -> !tv.get().edges( Direction.IN, "executes" ).hasNext() )
      .toList();
    return result.isEmpty() ? null : new TransformationNode( result.get( 0 ), graph );
  }

  public TransformationNode getSubTransformation( String name ) {
    List<Vertex> result = graph.traversal().V( vertex.id() )
      .out().has( "name", "Transformation" ).out( "typeconcept" ).has( "name", name )
      .filter( tv -> tv.get().edges( Direction.IN, "executes" ).hasNext() )
      .toList();
    return result.isEmpty() ? null : new TransformationNode( result.get( 0 ), graph );
  }

  public List<JobNode> getJobs() {
    List<Vertex> list = graph.traversal().V( vertex.id() )
      .repeat( __.out() ).emit( __.has( "name", "Job" ) ).until( __.loops().is( P.gte( 5 ) ) )
      .out( "typeconcept" ).dedup().toList();
    return wrapAs( list.iterator(), v -> new JobNode( v, graph ) );
  }

  public JobNode getJob( String name ) {
    List<Vertex> result = graph.traversal().V( vertex.id() )
      .repeat( __.out() ).emit( __.has( "name", "Job" ) ).until( __.loops().is( P.gte( 5 ) ) )
      .out().has( "name", name ).dedup().toList();
    return result.isEmpty() ? null : new JobNode( result.get( 0 ), graph );
  }

  public TransformationStepNode getStepNode( String transformationName, String stepName ) {
    return getTransformationStepNode( transformationName, stepName, v -> new TransformationStepNode( v, graph ) );
  }

  public List<FramedMetaverseNode> getEntities() {
    return wrapAsNodes( vertex.vertices( Direction.OUT ) );
  }

  public FramedMetaverseNode getEntity( String name ) {
    List<Vertex> result = graph.traversal().V( vertex.id() ).out().has( "name", name ).toList();
    return result.isEmpty() ? null : wrapNode( result.get( 0 ) );
  }

  public SelectValuesTransStepNode getSelectValuesStepNode() {
    return getTransformationStepNode( "Populate Table From File", "Select values",
      v -> new SelectValuesTransStepNode( v, graph ) );
  }

  public FileInputStepNode getFileInputStepNode( String transformationName, String stepName ) {
    return getTransformationStepNode( transformationName, stepName, v -> new FileInputStepNode( v, graph ) );
  }

  public FileInputStepNode getOldTextFileInputStepNode_filenameFromField() {
    return getTransformationStepNode( "Old Textfile input - filename from field", "Get Customers",
      v -> new FileInputStepNode( v, graph ) );
  }

  public FileInputStepNode getTextFileInputStepNode_filenameFromField() {
    return getTransformationStepNode( "Textfile input - filename from field", "Get Customers",
      v -> new FileInputStepNode( v, graph ) );
  }

  public List<DatasourceNode> getDatasourceNodes() {
    List<Vertex> list = graph.traversal().V( vertex.id() )
      .repeat( __.out() ).emit( __.has( "name", "Database Connection" ) ).until( __.loops().is( P.gte( 5 ) ) )
      .out().toList();
    return wrapAs( list.iterator(), v -> new DatasourceNode( v, graph ) );
  }

  public DatasourceNode getDatasourceNode( String name ) {
    List<Vertex> result = graph.traversal().V( vertex.id() )
      .out().has( "name", "External Connection" ).out().has( "name", "Database Connection" )
      .out( "typeconcept" ).has( "name", name ).toList();
    return result.isEmpty() ? null : new DatasourceNode( result.get( 0 ), graph );
  }

  public TextFileInputNode getTextFileInputNode() {
    return getTransformationStepNode( "Populate Table From File", "Sacramento crime stats 2006 file",
      v -> new TextFileInputNode( v, graph ) );
  }

  public TableOutputStepNode getTableOutputStepNode1() {
    return getTransformationStepNode( "Populate Table From File", "Demo table crime stats output [1]",
      v -> new TableOutputStepNode( v, graph ) );
  }

  public TableOutputStepNode getTableOutputStepNode2() {
    return getTransformationStepNode( "Populate Table From File", "Demo table crime stats output [2]",
      v -> new TableOutputStepNode( v, graph ) );
  }

  public TableInputStepNode getTableInputStepNode() {
    return getTransformationStepNode( "merge_join", "Table input", v -> new TableInputStepNode( v, graph ) );
  }

  public TextFileOutputStepNode getTextFileOutputStepNode( String transformationName, String stepName ) {
    return getTransformationStepNode( transformationName, stepName, v -> new TextFileOutputStepNode( v, graph ) );
  }

  public MergeJoinStepNode getMergeJoinStepNode() {
    return getTransformationStepNode( "merge_join", "Merge Join", v -> new MergeJoinStepNode( v, graph ) );
  }

  public StreamLookupStepNode getStreamLookupStepNode() {
    return getTransformationStepNode( "stream_lookup", "Stream lookup", v -> new StreamLookupStepNode( v, graph ) );
  }

  public CalculatorStepNode getCalculatorStepNode() {
    return getTransformationStepNode( "calculator", "Calculator", v -> new CalculatorStepNode( v, graph ) );
  }

  public CsvFileInputStepNode getCsvFileInputStepNode() {
    return getTransformationStepNode( "CSV Input", "CSV file input", v -> new CsvFileInputStepNode( v, graph ) );
  }

  public GroupByStepNode getGroupByStepNode() {
    return getTransformationStepNode( "group_by", "Group by", v -> new GroupByStepNode( v, graph ) );
  }

  public SplitFieldsStepNode getSplitFieldsStepNodeByName( String stepName ) {
    return getTransformationStepNode( "splitFields", stepName, v -> new SplitFieldsStepNode( v, graph ) );
  }

  public TransExecutorStepNode getTransExecutorStepNode() {
    return getTransformationStepNode( "trans-executor-parent", "Transformation Executor",
      v -> new TransExecutorStepNode( v, graph ) );
  }

  public RowsToResultStepNode getRowsToResultStepNode() {
    return getTransformationStepNode( "trans-executor-child", "Copy rows to result",
      v -> new RowsToResultStepNode( v, graph ) );
  }

  public FixedFileInputStepNode getFixedFileInputStepNode() {
    return getTransformationStepNode( "fixed_file_input", "Fixed file input",
      v -> new FixedFileInputStepNode( v, graph ) );
  }

  public GetXMLDataStepNode getGetXMLDataStepNode( String name ) {
    return getTransformationStepNode( "get_xml_data", name, v -> new GetXMLDataStepNode( v, graph ) );
  }

  public FilterRowsStepNode getFilterRowsStepNode( String name ) {
    List<Vertex> result = graph.traversal().V( vertex.id() )
      .repeat( __.out() )
      .emit( __.has( "type", "Transformation Step" ).has( "name", name ) )
      .until( __.loops().is( P.gte( 20 ) ) )
      .as( "step" ).in( "contains" ).has( "name", "filter_rows" ).select( "step" )
      .toList();
    return result.isEmpty() ? null : new FilterRowsStepNode( result.get( 0 ), graph );
  }

  public HttpClientStepNode getHttpClientStepNode() {
    return getTransformationStepNode( "HTTP_client", "HTTP Client", v -> new HttpClientStepNode( v, graph ) );
  }

  public HttpClientStepNode getHttpClientStepNode_urlFromField() {
    return getTransformationStepNode( "HTTP_client - url from field", "HTTP Client",
      v -> new HttpClientStepNode( v, graph ) );
  }

  public HttpPostStepNode getHttpPostStepNode() {
    return getTransformationStepNode( "HTTP_Post", "HTTP Post", v -> new HttpPostStepNode( v, graph ) );
  }

  public HttpPostStepNode getHttpPostStepNode_urlFromField() {
    return getTransformationStepNode( "HTTP_Post - url from field", "HTTP Post",
      v -> new HttpPostStepNode( v, graph ) );
  }

  public XMLOutputStepNode getXMLOutputStepNode( String transformationName, String stepName ) {
    return getTransformationStepNode( transformationName, stepName, v -> new XMLOutputStepNode( v, graph ) );
  }

  private <T extends TransformationStepNode> T getTransformationStepNode( String transformationName, String stepName,
                                                                          Function<Vertex, T> factory ) {
    List<Vertex> result = graph.traversal().V( vertex.id() )
      .out().has( "name", "Transformation" )
      .out().has( "name", "Transformation Step" )
      .out( "typeconcept" ).has( "name", stepName )
      .as( "step" ).in( "contains" ).has( "name", transformationName ).select( "step" )
      .toList();
    return result.isEmpty() ? null : factory.apply( result.get( 0 ) );
  }
}

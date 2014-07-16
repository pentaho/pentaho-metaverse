package com.pentaho.metaverse.graph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pentaho.metaverse.api.GraphConst;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class TestTinkerGraphMetaverseReader extends BaseGraphMetaverseReader {

  private static final long serialVersionUID = -68131573133600167L;

  public static String filePath;

  private static final Log logger = LogFactory.getLog( TestTinkerGraphMetaverseReader.class );

  private TinkerGraph graph;

  public TestTinkerGraphMetaverseReader() throws Exception {
    init();
  }

  @Override
  protected Graph getGraph() {
    return graph;
  }

  @Override
  public Log getLogger() {
    return logger;
  }

  private void init() {

    graph = new TinkerGraph();
    // these are the nodes
    Vertex textFile = createVertex( "data.txt", "file", "Text file: data.txt" );
    Vertex textField1 = createVertex( "data.txt;field1", "filefield", "Text field: IP Addr" );
    Vertex textField2 = createVertex( "data.txt;field2", "filefield", "Text field: Product" );
    Vertex trans1 =  createVertex( "trans1.ktr", "trans", "Transformation: trans1.ktr" );
    Vertex trans1Field1 = createVertex( "trans1.ktr;field1", "transfield", "Trans field: IP Addr" );
    Vertex trans1Field2 = createVertex( "trans1.ktr;field2", "transfield", "Trans field: Product" );
    Vertex trans1Field3 = createVertex( "trans1.ktr;field3", "transfield", "Trans field: City" );
    Vertex trans1Step1 = createVertex( "trans1.ktr;TextFileInput", "transstep", "Step: Read file" );
    Vertex trans1Step2 = createVertex( "trans1.ktr;Calc", "transstep", "Step: Calc city" );
    Vertex trans1Step3 = createVertex( "trans1.ktr;TableOutputStep", "transstep", "Step: Write temp table" );
    Vertex datasource1 = createVertex( "datasource1", "datasource", "Datasource: Postgres staging" );
    Vertex table1 = createVertex( "datasource1.table1", "table", "Table: temp table" );
    Vertex table1field1 = createVertex( "datasource1.table1.field1", "tablefield", "Table field: Tmp IP Addr" );
    Vertex table1field2 = createVertex( "datasource1.table1.field2", "tablefield", "Table field: Tmp Product" );
    Vertex table1field3 = createVertex( "datasource1.table1.field3", "tablefield", "Table field: Tmp City" );
    Vertex trans2 =  createVertex( "trans2.ktr", "trans", "Transformation: trans2.ktr" );
    Vertex trans2Field1 = createVertex( "trans2.ktr;field1", "transfield", "Trans field: IP Addr" );
    Vertex trans2Field2 = createVertex( "trans2.ktr;field2", "transfield", "Trans field: Product" );
    Vertex trans2Field3 = createVertex( "trans2.ktr;field3", "transfield", "Trans field: City" );
    Vertex trans2Field4 = createVertex( "trans2.ktr;fiel4", "transfield", "Transfield: Sales" );
    Vertex trans2Step1 = createVertex( "trans2.ktr;TableInputStep", "transstep", "Step: Table input step" );
    Vertex trans2Step2 = createVertex( "trans2.ktr;Javascript", "transstep", "Step: calc sales" );
    Vertex trans2Step3 = createVertex( "trans2.ktr;TableOuptutStep", "transstep", "Step: Write facttable" );
    Vertex table2 = createVertex( "datasource1.table2", "table", "Table: fact table" );
    Vertex job1 = createVertex( "job1.kjb", "job", "Job: job1.kjb" );
    Vertex table2field1 = createVertex( "datasource1.table2.field1", "tablefield", "Table field: Ip Addr" );
    Vertex table2field2 = createVertex( "datasource1.table2.field2", "tablefield", "Table field: Product" );
    Vertex table2field3 = createVertex( "datasource1.table2.field3", "tablefield", "Table field: City" );
    Vertex table2field4 = createVertex( "datasource1.table2.field4", "tablefield", "Table field: Sales" );

    // these are the links
    graph.addEdge( null, job1, trans1, GraphConst.LINK_EXECUTES );
    graph.addEdge( null, job1, trans2, GraphConst.LINK_EXECUTES );
    graph.addEdge( null, trans1, trans1Step1, GraphConst.LINK_EXECUTES );
    graph.addEdge( null, trans1, trans1Step2, GraphConst.LINK_EXECUTES );
    graph.addEdge( null, trans1, trans1Step3, GraphConst.LINK_EXECUTES );
    graph.addEdge( null, textFile, trans1Step1, GraphConst.LINK_READBY );
    graph.addEdge( null, textFile, textField1, GraphConst.LINK_CONTAINS );
    graph.addEdge( null, textFile, textField2, GraphConst.LINK_CONTAINS );
    graph.addEdge( null, trans1Step1, trans1Field1, GraphConst.LINK_CREATES );
    graph.addEdge( null, trans1Step1, trans1Field2, GraphConst.LINK_CREATES );
    graph.addEdge( null, textField1, trans1Field1, GraphConst.LINK_POPULATES );
    graph.addEdge( null, textField2, trans1Field2, GraphConst.LINK_POPULATES );
    graph.addEdge( null, trans1Step2, trans1Field3, GraphConst.LINK_CREATES );
    graph.addEdge( null, trans1Step3, table1, GraphConst.LINK_WRITESTO );
    graph.addEdge( null, datasource1, table1, GraphConst.LINK_CONTAINS );
    graph.addEdge( null, table1, table1field1, GraphConst.LINK_CONTAINS );
    graph.addEdge( null, table1, table1field2, GraphConst.LINK_CONTAINS );
    graph.addEdge( null, table1, table1field3, GraphConst.LINK_CONTAINS );
    graph.addEdge( null, trans1Field1, table1field1, GraphConst.LINK_POPULATES );
    graph.addEdge( null, trans1Field2, table1field2, GraphConst.LINK_POPULATES );
    graph.addEdge( null, trans1Field3, table1field3, GraphConst.LINK_POPULATES );

    graph.addEdge( null, trans2, trans2Step1, GraphConst.LINK_EXECUTES );
    graph.addEdge( null, trans2, trans2Step2, GraphConst.LINK_EXECUTES );
    graph.addEdge( null, trans2, trans2Step3, GraphConst.LINK_EXECUTES );
    graph.addEdge( null, table1, trans2Step1, GraphConst.LINK_READBY );
    graph.addEdge( null, trans2Step1, trans2Field1, GraphConst.LINK_CREATES );
    graph.addEdge( null, trans2Step1, trans2Field2, GraphConst.LINK_CREATES );
    graph.addEdge( null, trans2Step1, trans2Field3, GraphConst.LINK_CREATES );
    graph.addEdge( null, table1field1, trans2Field1, GraphConst.LINK_POPULATES );
    graph.addEdge( null, table1field2, trans2Field2, GraphConst.LINK_POPULATES );
    graph.addEdge( null, table1field3, trans2Field3, GraphConst.LINK_POPULATES );
    graph.addEdge( null, trans2Step2, trans2Field4, GraphConst.LINK_CREATES );
    graph.addEdge( null, trans2Step3, table2, GraphConst.LINK_WRITESTO );
    graph.addEdge( null, datasource1, table2, GraphConst.LINK_CONTAINS );
    graph.addEdge( null, table2, table2field1, GraphConst.LINK_CONTAINS );
    graph.addEdge( null, table2, table2field2, GraphConst.LINK_CONTAINS );
    graph.addEdge( null, table2, table2field3, GraphConst.LINK_CONTAINS );
    graph.addEdge( null, table2, table2field4, GraphConst.LINK_CONTAINS );
    graph.addEdge( null, trans2Field1, table2field1, GraphConst.LINK_POPULATES );
    graph.addEdge( null, trans2Field2, table2field2, GraphConst.LINK_POPULATES );
    graph.addEdge( null, trans2Field3, table2field3, GraphConst.LINK_POPULATES );
    graph.addEdge( null, trans2Field4, table2field4, GraphConst.LINK_POPULATES );

  }

  public void dumpGraph( Graph g, String fileName ) {
    // dump the graph to XML for visual inspection etc
    GraphMLWriter writer = new GraphMLWriter();
    File file = new File( fileName );
    OutputStream outputStream = null;
    try {
      outputStream = new FileOutputStream( file );
      writer.outputGraph( g, outputStream );
    } catch ( Exception e ) {
    } finally {
      try {
        outputStream.close();
      } catch ( IOException e ) {
      }
    }

  }

  private Vertex createVertex( String id, String type, String label ) {
    Vertex v = graph.addVertex( id );
    v.setProperty( "type", type );
    v.setProperty( "name", label );
    return v;
  }

}

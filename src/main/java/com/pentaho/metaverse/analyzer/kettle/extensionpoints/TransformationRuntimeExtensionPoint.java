package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.MetaverseTransientNode;
import com.pentaho.metaverse.analyzer.kettle.DatabaseConnectionAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.JobAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.TransformationAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.step.IStepAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.step.StepAnalyzerProvider;
import com.pentaho.metaverse.analyzer.kettle.step.TableOutputStepAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.step.TextFileInputStepAnalyzer;
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.graph.BlueprintsGraphMetaverseReader;
import com.pentaho.metaverse.impl.AnalysisContext;
import com.pentaho.metaverse.impl.DocumentController;
import com.pentaho.metaverse.impl.MetaverseBuilder;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.impl.MetaverseObjectFactory;
import com.pentaho.metaverse.impl.Namespace;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import org.apache.commons.io.FileUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.version.BuildVersion;
import org.pentaho.platform.api.metaverse.IAnalysisContext;
import org.pentaho.platform.api.metaverse.IDocumentAnalyzer;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Created by mburgess on 8/25/14.
 */
@ExtensionPoint(
    description = "Transformation Runtime metadata extractor",
    extensionPointId = "TransformationStartThreads",
    id = "transRuntimeMetaverse" )
public class TransformationRuntimeExtensionPoint implements ExtensionPointInterface, TransListener {

  private Graph g;
  private DocumentController dc;
  private MetaverseBuilder metaverseBuilder;
  private IMetaverseReader metaverseReader;
  private TransformationAnalyzer transAnalyzer;
  private Trans trans;

  public TransformationRuntimeExtensionPoint() {

    g = new IdGraph<KeyIndexableGraph>( new TinkerGraph() );

    dc = new DocumentController();
    metaverseBuilder = new MetaverseBuilder( g );
    metaverseBuilder.setMetaverseObjectFactory( new MetaverseObjectFactory() );

    metaverseReader = new BlueprintsGraphMetaverseReader( g );

    dc.setMetaverseBuilder( metaverseBuilder );

    final DatabaseConnectionAnalyzer dba = new DatabaseConnectionAnalyzer();

    final TextFileInputStepAnalyzer tfia = new TextFileInputStepAnalyzer() {
      {
        setDatabaseConnectionAnalyzer( dba );
      }
    };

    final TableOutputStepAnalyzer tfoa = new TableOutputStepAnalyzer() {
      {
        setDatabaseConnectionAnalyzer( dba );
      }
    };

    final StepAnalyzerProvider ksap = new StepAnalyzerProvider();
    ksap.setStepAnalyzers( new HashSet<IStepAnalyzer>() {
      {
        add( tfia );
        add( tfoa );
      }
    } );
    final TransformationAnalyzer ta = new TransformationAnalyzer() {
      {
        setStepAnalyzerProvider( ksap );
      }
    };
    transAnalyzer = ta;
    final JobAnalyzer ja = new JobAnalyzer();
    dc.setDocumentAnalyzers( new HashSet<IDocumentAnalyzer>() {
      {
        add( ta );
        add( ja );
      }
    } );

  }

  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
    trans = (Trans) o;

    TransMeta transMeta = trans.getTransMeta();

    IMetaverseObjectFactory objectFactory = metaverseBuilder.getMetaverseObjectFactory();

    IAnalysisContext context = new AnalysisContext( DictionaryConst.CONTEXT_RUNTIME, trans );

    IMetaverseNode runtimeInfo = addRuntimeNode( trans, context, objectFactory );
    IMetaverseNode executionNode = addExecutionEngineNode( trans, objectFactory );

    metaverseBuilder.addLink( runtimeInfo, "executed by", executionNode );

    IMetaverseDocument transDoc = objectFactory.createDocumentObject();
    IMetaverseNode locatorNode = new MetaverseTransientNode();
    locatorNode.setProperty( DictionaryConst.PROPERTY_NAME, "EXTENSION-POINT-LOCATOR" );
    locatorNode.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_LOCATOR );
    locatorNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_LOCATOR );
    INamespace parentNamespace = new Namespace( locatorNode.getLogicalId() );

    File f = new File( trans.getFilename() );

    String filePath = null;
    try {
      filePath = f.getCanonicalPath();
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    transDoc.setNamespace( parentNamespace );
    transDoc.setContent( trans );
    transDoc.setStringID( filePath );
//    transDoc.setName( transMeta.getFilename() );
    transDoc.setName( transMeta.getName() );
    transDoc.setExtension( "ktr" );
    transDoc.setMimeType( "text/xml" );
    transDoc.setType( DictionaryConst.NODE_TYPE_TRANS );
    transDoc.setProperty( DictionaryConst.PROPERTY_PATH, filePath );
    transDoc.setProperty( DictionaryConst.PROPERTY_NAMESPACE, locatorNode.getLogicalId() );
    transDoc.setProperty( "locator", locatorNode.getName() );
    transDoc.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );
    transDoc.setContext( context );

    IMetaverseComponentDescriptor descriptor = new MetaverseComponentDescriptor(
        transMeta.getName(),
        DictionaryConst.NODE_TYPE_TRANS,
        parentNamespace,
        context );

    try {
      IMetaverseNode node = transAnalyzer.analyze( descriptor, transDoc );
      metaverseBuilder.addLink( runtimeInfo, "executed", node );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }

    trans.addTransListener( this );
  }

  private IMetaverseNode addRuntimeNode( Trans trans, IAnalysisContext context,
      IMetaverseObjectFactory objectFactory ) {
    IMetaverseNode runtimeNode = new MetaverseTransientNode();
    runtimeNode.setProperty( DictionaryConst.PROPERTY_NAME, DictionaryConst.NODE_TYPE_RUNTIME );
    runtimeNode.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_RUNTIME );
    runtimeNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_LOCATOR );

    long timestamp = new Date().getTime();
    String runtimeId = trans.getLogChannelId() == null ? UUID.randomUUID().toString() : trans.getLogChannelId();
    IMetaverseComponentDescriptor runTimeDescriptor = new MetaverseComponentDescriptor(
        runtimeId,
        DictionaryConst.NODE_TYPE_RUNTIME,
        new Namespace( runtimeNode.getLogicalId() ),
        context );

    // add a runtime node so we can identify them
    IMetaverseNode runtimeInfo = objectFactory.createNodeObject(
      runtimeId, runTimeDescriptor.getName(), runTimeDescriptor.getType() );

    runtimeInfo.setProperty( "executionDate", String.valueOf( timestamp ) );
    runtimeInfo.setProperty( DictionaryConst.PROPERTY_PATH, trans.getFilename() );
    runtimeInfo.setProperty( DictionaryConst.PROPERTY_LOGICAL_ID, runtimeNode.getLogicalId() );
    runtimeInfo.setProperty( "user", trans.getExecutingUser() );

    metaverseBuilder.addNode( runtimeInfo );
    List<String> vars = trans.getTransMeta().getUsedVariables();
    for ( String var : vars ) {
      String value = trans.getVariable( var );
      runtimeInfo.setProperty( "parameter_" + var, value );
    }

    return runtimeInfo;

  }

  private IMetaverseNode addExecutionEngineNode( Trans trans, IMetaverseObjectFactory objectFactory ) {
    IMetaverseNode pdi = new MetaverseTransientNode();
    pdi.setProperty( DictionaryConst.PROPERTY_NAME, "Pentaho Data Integration" );
    pdi.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_EXECUTION_ENGINE );

    IMetaverseNode node = objectFactory.createNodeObject(
      new Namespace( pdi.getLogicalId() ),
      "Pentaho Data Integration [" + trans.getExecutingServer() + "]",
      DictionaryConst.NODE_TYPE_EXECUTION_ENGINE );

    String version = BuildVersion.getInstance().getVersion();
    node.setProperty( "version", version );
    metaverseBuilder.addNode( node );
    return node;
  }

  /**
   * This transformation started
   *
   * @param trans
   * @throws org.pentaho.di.core.exception.KettleException
   */
  @Override
  public void transStarted( Trans trans ) throws KettleException {
    // Do nothing here, we've already done setup as part of the extension point
  }

  /**
   * This transformation went from an in-active to an active state.
   *
   * @param trans
   * @throws org.pentaho.di.core.exception.KettleException
   */
  @Override
  public void transActive( Trans trans ) {
    // Do nothing here
  }

  /**
   * The transformation has finished.
   *
   * @param trans
   * @throws org.pentaho.di.core.exception.KettleException
   */
  @Override
  public void transFinished( Trans trans ) throws KettleException {

    // get the vertex that represents the runtime node
    Vertex vertex = g.getVertex( trans.getLogChannelId() );

    vertex.setProperty( "numberOfErrors", String.valueOf( trans.getResult().getNrErrors() ) );
    vertex.setProperty( "rowsWritten", String.valueOf( trans.getResult().getNrLinesWritten() ) );
    vertex.setProperty( "rowsRead", String.valueOf( trans.getResult().getNrLinesRead() ) );
    vertex.setProperty( "rowsInput", String.valueOf( trans.getResult().getNrLinesInput() ) );
    vertex.setProperty( "rowsOutput", String.valueOf( trans.getResult().getNrLinesOutput() ) );

    File exportFile = new File( trans.getTransMeta().getName() + " - export.graphml" );
    BlueprintsGraphMetaverseReader metaverseReader = new BlueprintsGraphMetaverseReader( g );
    try {
      FileUtils.writeStringToFile( exportFile, metaverseReader.exportToXml(), "UTF-8" );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }
}

package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.DatabaseConnectionAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.JobAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.TransformationAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.step.IStepAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.step.StepAnalyzerProvider;
import com.pentaho.metaverse.analyzer.kettle.step.TableOutputStepAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.step.TextFileInputStepAnalyzer;
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.graph.BlueprintsGraphMetaverseReader;
import com.pentaho.metaverse.impl.DocumentController;
import com.pentaho.metaverse.impl.MetaverseBuilder;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.impl.MetaverseObjectFactory;
import com.pentaho.metaverse.impl.NamespaceFactory;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import org.apache.commons.io.FileUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
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

/**
 * Created by mburgess on 8/25/14.
 */
@ExtensionPoint(
    description = "Transformation Runtime metadata extractor",
    extensionPointId = "TransformationStartThreads",
    id = "transRuntimeMetaverse" )
public class TransformationRuntimeExtensionPoint implements ExtensionPointInterface {

  private Graph g;
  private DocumentController dc;
  private MetaverseBuilder metaverseBuilder;
  private IMetaverseReader metaverseReader;
  private TransformationAnalyzer transAnalyzer;

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
    Trans trans = (Trans) o;

    TransMeta transMeta = trans.getTransMeta();
    List<String> variables = transMeta.getUsedVariables();

    IMetaverseObjectFactory objectFactory = metaverseBuilder.getMetaverseObjectFactory();

    IMetaverseNode runtimeInfo = addRuntimeNode( trans, objectFactory );
    IMetaverseNode executionNode = addExecutionEngineNode( trans, objectFactory );

    metaverseBuilder.addLink( runtimeInfo, "executed by", executionNode );

    IMetaverseDocument transDoc = objectFactory.createDocumentObject();

    INamespace parentNamespace =
        new NamespaceFactory().createNameSpace( null, "EXTENSION-POINT-LOCATOR", DictionaryConst.NODE_TYPE_LOCATOR );

    File f = new File( trans.getFilename() );

    String filePath = null;
    try {
      filePath = f.getCanonicalPath();
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    transDoc.setNamespace( parentNamespace );
    transDoc.setContent( transMeta );
    transDoc.setStringID( filePath );
    transDoc.setName( transMeta.getFilename() );
    transDoc.setExtension( "ktr" );
    transDoc.setMimeType( "text/xml" );
    transDoc.setProperty( DictionaryConst.PROPERTY_PATH, filePath );
    transDoc.setContext( DictionaryConst.CONTEXT_RUNTIME );

    IMetaverseComponentDescriptor descriptor = new MetaverseComponentDescriptor(
        transMeta.getName(),
        DictionaryConst.NODE_TYPE_TRANS,
        parentNamespace,
        DictionaryConst.CONTEXT_RUNTIME );

    try {
      IMetaverseNode node = transAnalyzer.analyze( descriptor, transDoc );
      metaverseBuilder.addLink( runtimeInfo, "executed", node );
      File exportFile = new File( transMeta.getName() + " - export.graphml" );
      FileUtils.writeStringToFile( exportFile, metaverseReader.exportToXml(), "UTF-8" );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  private IMetaverseNode addRuntimeNode( Trans trans, IMetaverseObjectFactory objectFactory ) {
    INamespace ns = new NamespaceFactory().createNameSpace(
      null, DictionaryConst.NODE_TYPE_RUNTIME, DictionaryConst.NODE_TYPE_RUNTIME );

    long timestamp = new Date().getTime();
    IMetaverseComponentDescriptor runTimeDescriptor = new MetaverseComponentDescriptor(
      trans.getName() + " - " + timestamp,
      DictionaryConst.NODE_TYPE_RUNTIME,
      ns,
      DictionaryConst.CONTEXT_RUNTIME );

    // add a runtime node so we can identify them
    IMetaverseNode runtimeInfo = objectFactory.createNodeObject(
      runTimeDescriptor.getStringID(), runTimeDescriptor.getName(), runTimeDescriptor.getType() );

    runtimeInfo.setProperty( "executionDate", String.valueOf( timestamp ) );
    runtimeInfo.setProperty( DictionaryConst.PROPERTY_PATH, trans.getFilename() );
    runtimeInfo.setProperty( "user", trans.getExecutingUser() );

    metaverseBuilder.addNode( runtimeInfo );

    String[] vars = trans.listVariables();
    for ( String var : vars ) {
      String value = trans.getVariable( var );
      runtimeInfo.setProperty( "parameter_" + var, value );
    }

    return runtimeInfo;

  }

  private IMetaverseNode addExecutionEngineNode( Trans trans, IMetaverseObjectFactory objectFactory ) {
    INamespace ns = new NamespaceFactory().createNameSpace(
      null, "Pentaho Data Integration", DictionaryConst.NODE_TYPE_EXECUTION_ENGINE );

    IMetaverseNode node = objectFactory.createNodeObject( ns.getNamespaceId(),
      "Pentaho Data Integration [" +  trans.getExecutingServer() + "]",
      DictionaryConst.NODE_TYPE_EXECUTION_ENGINE );

    metaverseBuilder.addNode( node );
    return node;
  }


}

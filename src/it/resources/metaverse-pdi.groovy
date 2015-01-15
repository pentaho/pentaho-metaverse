import com.pentaho.metaverse.analyzer.kettle.step.mergejoin.MergeJoinStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.numberrange.NumberRangeStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.selectvalues.SelectValuesStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.tableoutput.TableOutputStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.textfileinput.TextFileInputStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.valuemapper.ValueMapperStepAnalyzer
import org.pentaho.platform.api.metaverse.*
import com.pentaho.metaverse.api.*
import com.pentaho.metaverse.graph.*
import com.pentaho.metaverse.impl.*
import com.pentaho.metaverse.locator.*
import com.pentaho.metaverse.service.*
import com.pentaho.metaverse.analyzer.kettle.*
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.*
import com.pentaho.metaverse.analyzer.kettle.step.*
import com.pentaho.metaverse.analyzer.kettle.jobentry.*
import com.pentaho.dictionary.*
import org.pentaho.di.cluster.*
import org.pentaho.di.core.*
import org.pentaho.di.core.database.*
import org.pentaho.di.core.exception.*
import org.pentaho.di.core.extension.*
import org.pentaho.di.core.gui.*
import org.pentaho.di.core.logging.*
import org.pentaho.di.core.plugins.*
import org.pentaho.di.core.row.*
import org.pentaho.di.core.variables.*
import org.pentaho.di.core.vfs.*
import org.pentaho.di.job.*
import org.pentaho.di.repository.*
import org.pentaho.di.trans.*
import org.pentaho.di.trans.step.*
import org.pentaho.di.ui.spoon.*
import org.pentaho.di.ui.spoon.delegates.*
import org.pentaho.di.ui.spoon.trans.*
import org.pentaho.groovy.ui.spoon.*
import org.pentaho.groovy.ui.spoon.repo.*

i:{

  try {
    c = Class.forName('com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransformationRuntimeExtensionPoint')
    ExtensionPointPluginType.getInstance().registerCustom( c, "custom", "transRuntimeMetaverse", "TransformationStartThreads", "no description", null )
  }
  catch(e) {
    println 'Extension point(s) not loaded'
  }

  PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );
  KettleEnvironment.init()
  Gremlin.load()
  mtos = [:]
  mcs = MetaverseCompletionService.instance

  trans = [] as Trans[]


  loadIntegrationTestObjects = { obj,shell ->
  obj.with {
  	g = new TinkerGraph()
  	dc = new DocumentController()
  	mb = new MetaverseBuilder(g)
  	dc.setMetaverseBuilder(mb)

    dba = new DatabaseConnectionAnalyzer()
    dbap = new DatabaseConnectionAnalyzerProvider()
    dbap.setDatabaseConnectionAnalyzers([dba] as Set)

    //**********************************************************************
    // Set up the step analyzers
  	tfia = new TextFileInputStepAnalyzer()
  	tfia.setDatabaseConnectionAnalyzerProvider(dbap)

  	tfoa = new TableOutputStepAnalyzer()
  	tfoa.setDatabaseConnectionAnalyzerProvider(dbap)

    mergeJoinAnalyzer = new MergeJoinStepAnalyzer()
    mergeJoinAnalyzer.setDatabaseConnectionAnalyzerProvider(dbap)

    numberRangeAnalyzer = new NumberRangeStepAnalyzer()
    numberRangeAnalyzer.setDatabaseConnectionAnalyzerProvider(dbap)

    selectValuesAnalyzer = new SelectValuesStepAnalyzer()
    selectValuesAnalyzer.setDatabaseConnectionAnalyzerProvider(dbap)

    tableInputAnalyzer = new TableInputStepAnalyzer()
    tableInputAnalyzer.setDatabaseConnectionAnalyzerProvider(dbap)

    valueMapperAnalyzer = new ValueMapperStepAnalyzer()
    valueMapperAnalyzer.setDatabaseConnectionAnalyzerProvider(dbap)
    //**********************************************************************

  	ksap = new StepAnalyzerProvider()
  	ksap.setStepAnalyzers([tfia,tfoa, mergeJoinAnalyzer, numberRangeAnalyzer, selectValuesAnalyzer, tableInputAnalyzer, valueMapperAnalyzer] as Set)

  	ta = new TransformationAnalyzer()
  	ta.setStepAnalyzerProvider(ksap)

  	ja = new JobAnalyzer()
  	dc.setDocumentAnalyzers([ta,ja] as Set)
  	
    dl = new FileSystemLocator()
  	dl.with {
  		addDocumentListener(dc)
  		setRepositoryId('FILE_SYSTEM_REPO')
  		setMetaverseBuilder(dc)
  		setRootFolder('src/it/resources/repo')
  	}
  	gw = new GraphMLWriter()
  	gsonw = new GraphSONWriter()
    g2 = new TinkerGraph()
    gr = new GraphMLReader(g2)
  }

  shell.g = obj.g
  shell.g2 = obj.g2
  shell.dc = obj.dc
  shell.mb = obj.mb
  shell.dl = obj.dl
  shell.ta = obj.ta
  shell.gw = obj.gw
  shell.gr = obj.gr

  // Helper constants (to save typing)
  shell.TRANS = DictionaryConst.NODE_TYPE_TRANS
  shell.STEP = DictionaryConst.NODE_TYPE_TRANS_STEP
  shell.JOB = DictionaryConst.NODE_TYPE_JOB
  shell.JOBENTRY = DictionaryConst.NODE_TYPE_JOB_ENTRY
}
  getPathFormatter    = { def x = [{"${it.name} (${it.type})"}]; 10.times {x << {"<-[ ${it.label} ]-"}; x<<{"${it.name} (${it.type})"}}; x.toArray() as Closure[]}
  getRevPathFormatter = { def x = [{"${it.name} (${it.type})"}]; 10.times {x << {"-[ ${it.label} ]->"}; x<<{"${it.name} (${it.type})"}}; x.toArray() as Closure[]}

  loadIT = {
    loadIntegrationTestObjects(mtos, this)
    'Integration test objects loaded'
  }
  scanIT = {
    dl?.startScan()
    mcs?.waitTillEmpty()
    'Scan complete'
  }

  loadFolder = { folder ->
    dl.rootFolder = folder
    ( folder as File ).eachFileMatch(groovy.io.FileType.FILES, ~/.*\.ktr/) { ktr ->
      ktr.withInputStream { i ->
        Variables vars = new Variables()
        vars.setVariable( "testTransParam3", "demo" )
        tm = new TransMeta( i, null, true, vars, null )
        tm.filename = tm.name
        Trans t = new Trans( tm, null, tm.name, folder, ktr.absolutePath )
        t.setVariable( "testTransParam3", "demo" );
        t.setVariable( "Internal.Transformation.Filename.Directory", folder);
        tm.setVariable( "testTransParam3", "demo" );
        tm.setVariable( "Internal.Transformation.Filename.Directory", folder);
        trans += t
      }
    }
    "loaded repo from ${folder}" 
  }
  loadDemo = {
    loadFolder 'src/it/resources/repo/demo'
  }

  loadSamples = {
    loadFolder 'src/it/resources/repo/samples'
  }

  loadValidation = {
    loadFolder 'src/it/resources/repo/validation'
  }

  loadGraph = { fname ->
    new File(fname).withInputStream { i -> gr.inputGraph(i) }
  }

  // Measures the number of seconds it takes to run the given closure 
  timeToRun = { closureToMeasure ->
    now = System.currentTimeMillis()
    closureToMeasure()
    then = System.currentTimeMillis()
    "${(then-now) / 1000.0f} s"
  }

  ls = { dir ->
    new File(dir ?:'.').list()
  }

  loadIT()
  'Gremlin-Kettle-Metaverse initialized successfully'
}

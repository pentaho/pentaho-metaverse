import com.pentaho.metaverse.analyzer.kettle.step.csvfileinput.CsvFileInputStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.mergejoin.MergeJoinStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.numberrange.NumberRangeStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.selectvalues.SelectValuesStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.tableoutput.TableOutputStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.textfileinput.TextFileInputStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.valuemapper.ValueMapperStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.calculator.CalculatorStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.streamlookup.StreamLookupStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.excelinput.ExcelInputStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.groupby.GroupByStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.calculator.StringOperationsStepAnalyzer
import com.pentaho.metaverse.analyzer.kettle.step.stringscut.StringsCutStepAnalyzer

import com.pentaho.metaverse.api.*
import com.pentaho.metaverse.api.*
import com.pentaho.metaverse.client.*
import com.pentaho.metaverse.graph.*
import com.pentaho.metaverse.impl.*
import com.pentaho.metaverse.locator.*
import com.pentaho.metaverse.util.*
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

i:
{

  try {
    c = Class.forName('com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransformationRuntimeExtensionPoint')
    ExtensionPointPluginType.getInstance().registerCustom(c, "custom", "transRuntimeMetaverse", "TransformationStartThreads", "no description", null)
  }
  catch (e) {
    println 'Extension point(s) not loaded'
  }
  // Check for KETTLE_PLUGIN_BASE_FOLDERS, default to ${user.home}/pdi-ee-6.0-SNAPSHOT/pdi-ee/data-integration/plugins
  System.setProperty('KETTLE_PLUGIN_BASE_FOLDERS',
    System.getProperty('KETTLE_PLUGIN_BASE_FOLDERS') ?:
      "${System.getProperty('user.home')}/pdi-ee-6.0-SNAPSHOT/pdi-ee/data-integration/plugins")

  PluginRegistry.addPluginType(ExtensionPointPluginType.getInstance());
  KettleClientEnvironment.instance.setClient( KettleClientEnvironment.ClientType.PAN );
  KettleEnvironment.init(false)
  Gremlin.load()
  // Add custom Gremlin steps for convenience
  Gremlin.defineStep('steps', [Vertex, Pipe],
    { name ->
      _().has('type', DictionaryConst.NODE_TYPE_TRANS_STEP)
    }
  )
  Gremlin.defineStep('step', [Vertex, Pipe],
    { name ->
      _().steps().has('name', name)
    }
  )
  Gremlin.defineStep('myTrans', [Vertex, Pipe],
    {
      _().in.loop(1) { it.loops < 10 } { it.object.type == 'Transformation' }
    }
  )
  Gremlin.defineStep('creator', [Vertex, Pipe],
    { field ->
      _().in('hops_to').loop(1) { it.loops < 10 } {
        it.object != null
      }.as('step').out('creates').has('name', field).back("step")
    }
  )
  Gremlin.defineStep('origin', [Vertex, Pipe],
    { transName, stepName ->
      _().and(_().has("name", stepName), _().in("contains").and(_().has("name", transName), _().has("type", "Transformation")))
    }
  )
  mtos = [:]
  mcs = MetaverseCompletionService.instance

  trans = [] as Trans[]


  loadIntegrationTestObjects = { obj, shell ->
    obj.with {
      g = new TinkerGraph()
      dc = new DocumentController()
      mb = new MetaverseBuilder(g)
      dc.setMetaverseBuilder(mb)

      stepDbAnalyzer = new StepDatabaseConnectionAnalyzer()
      stepDbAnalyzer.setMetaverseBuilder(mb)
      //**********************************************************************
      // Set up the step analyzers
      tfia = new TextFileInputStepAnalyzer()
      cfia = new CsvFileInputStepAnalyzer()

      tfoa = new TableOutputStepAnalyzer()
      tfoa.setConnectionAnalyzer(stepDbAnalyzer)

      mergeJoinAnalyzer = new MergeJoinStepAnalyzer()

      numberRangeAnalyzer = new NumberRangeStepAnalyzer()

      selectValuesAnalyzer = new SelectValuesStepAnalyzer()

      tableInputAnalyzer = new TableInputStepAnalyzer()
      tableInputAnalyzer.setConnectionAnalyzer(stepDbAnalyzer)

      valueMapperAnalyzer = new ValueMapperStepAnalyzer()

      streamLookupAnalyzer = new StreamLookupStepAnalyzer()

      calculatorAnalyzer = new CalculatorStepAnalyzer()

      excelInputAnalyzer = new ExcelInputStepAnalyzer()

      groupByAnalyzer = new GroupByStepAnalyzer()
      
      stringOperationsAnalyzer = new StringOperationsStepAnalyzer()
      
      stringsCutAnalyzer = new StringsCutStepAnalyzer()

      //**********************************************************************

      ksap = new StepAnalyzerProvider()
      ksap.setStepAnalyzers(
        [tfia, tfoa, cfia,
         mergeJoinAnalyzer, numberRangeAnalyzer, selectValuesAnalyzer, tableInputAnalyzer, valueMapperAnalyzer,
         streamLookupAnalyzer, calculatorAnalyzer, groupByAnalyzer, excelInputAnalyzer] as List)

      ta = new TransformationAnalyzer()
      ta.setStepAnalyzerProvider(ksap)

      ja = new JobAnalyzer()
      dc.setDocumentAnalyzers([ta, ja] as List)

      dl = new FileSystemLocator()
      dl.with {
        addDocumentListener(dc)
        setRepositoryId('FILE_SYSTEM_REPO')
        setMetaverseBuilder(dc)
        setRootFolder('src/it/resources/repo')
      }
      gw = new GraphMLWriter()
      gson = new GraphSONReader()
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
    shell.gson = obj.gson
    shell.gsonw = obj.gsonw
    shell.gr = obj.gr

    MetaverseUtil.documentController = shell.dc

    // Helper constants (to save typing)
    shell.TRANS = DictionaryConst.NODE_TYPE_TRANS
    shell.STEP = DictionaryConst.NODE_TYPE_TRANS_STEP
    shell.JOB = DictionaryConst.NODE_TYPE_JOB
    shell.JOBENTRY = DictionaryConst.NODE_TYPE_JOB_ENTRY
    shell.NAME = DictionaryConst.PROPERTY_NAME
    shell.DERIVES = DictionaryConst.LINK_DERIVES
  }
  getPathFormatter = {
    def x = [{ "${it.name} (${it.type})" }];
    10.times { x << { "<-[ ${it.label} ]-" }; x << { "${it.name} (${it.type})" } }; x.toArray() as Closure[]
  }
  getRevPathFormatter = {
    def x = [{ "${it.name} (${it.type})" }];
    10.times { x << { "-[ ${it.label} ]->" }; x << { "${it.name} (${it.type})" } }; x.toArray() as Closure[]
  }

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
    file = (folder as File)
    if (file.isDirectory()) {
      dl.rootFolder = folder
      file.eachFileMatch(groovy.io.FileType.FILES, ~/.*\.ktr/) { ktr ->
        ktr.withInputStream { i ->
          Variables vars = new Variables()
          vars.setVariable("testTransParam3", "demo")
          tm = new TransMeta(i, null, true, vars, null)
          tm.filename = tm.name
          Trans t = new Trans(tm, null, tm.name, folder, ktr.absolutePath)
          t.setVariable("testTransParam3", "demo");
          t.setVariable("Internal.Transformation.Filename.Directory", folder);
          tm.setVariable("testTransParam3", "demo");
          tm.setVariable("Internal.Transformation.Filename.Directory", folder);
          trans += t
        }
      }
    } else {
      dl.rootFolder = file.parent
      file.withInputStream { i ->
        Variables vars = new Variables()
        tm = new TransMeta(i, null, true, vars, null)
        tm.filename = tm.name
        Trans t = new Trans(tm, null, tm.name, folder, file.absolutePath)
        t.setVariable("Internal.Transformation.Filename.Directory", folder);
        tm.setVariable("Internal.Transformation.Filename.Directory", folder);
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

  saveGraph = { fname ->
    new File(fname).withOutputStream { i -> gw.outputGraph(g, i) }
  }

  lineageClient = { fname ->
    tm = new TransMeta(fname)
    doc = MetaverseUtil.createDocument(new Namespace("SPOON"), tm, tm.filename, tm.name, 'ktr', URLConnection.fileNameMap.getContentTypeFor(tm.filename))
    MetaverseUtil.addLineageGraph(doc, graph = new TinkerGraph())
    new LineageClient()
  }

  // Measures the number of seconds it takes to run the given closure
  timeToRun = { closureToMeasure ->
    now = System.currentTimeMillis()
    closureToMeasure()
    then = System.currentTimeMillis()
    "${(then - now) / 1000.0f} s"
  }

  ls = { dir ->
    new File(dir ?: '.').list()
  }



  loadIT()
  'Gremlin-Kettle-Metaverse initialized successfully'
}

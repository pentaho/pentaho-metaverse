package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.metaverse.analyzer.kettle.BaseKettleMetaverseComponent;
import org.pentaho.di.trans.step.BaseStepMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The KettleStepAnalyzerProvider maintains a collection of analyzer objects capable of analyzing various PDI step
 */
public class StepAnalyzerProvider extends BaseKettleMetaverseComponent implements IStepAnalyzerProvider {

  /**
   * The set of step analyzers.
   */
  protected Set<IStepAnalyzer> stepAnalyzers = new HashSet<IStepAnalyzer>();

  /**
   * The analyzer type map associates step meta classes with analyzers for those classes
   */
  protected Map<Class<? extends BaseStepMeta>, Set<IStepAnalyzer>> analyzerTypeMap =
      new HashMap<Class<? extends BaseStepMeta>, Set<IStepAnalyzer>>();

  /**
   * Returns all registered step analyzers
   * @return a set of step analyzers
   */
  @Override
  public Set<IStepAnalyzer> getAnalyzers() {
    return stepAnalyzers;
  }

  /**
   * Returns the set of analyzers for step with the specified classes
   *
   * @param types a set of classes corresponding to step for which to retrieve the analyzers
   * @return a set of analyzers that can process the specified step
   */
  @Override public Set<IStepAnalyzer> getAnalyzers( Set<Class<?>> types ) {
    Set<IStepAnalyzer> stepAnalyzers = getAnalyzers();
    if ( types != null ) {
      final Set<IStepAnalyzer> specificStepAnalyzers = new HashSet<IStepAnalyzer>();
      for ( Class<?> clazz : types ) {
        if ( analyzerTypeMap.containsKey( clazz ) ) {
          specificStepAnalyzers.addAll( analyzerTypeMap.get( clazz ) );
        }
      }
      stepAnalyzers = specificStepAnalyzers;
    }
    return stepAnalyzers;
  }

  /**
   * Sets the collection of step analyzers used to analyze PDI step
   * @param analyzers
   */
  public void setStepAnalyzers( Set<IStepAnalyzer> analyzers ) {
    stepAnalyzers = analyzers;
    loadAnalyzerTypeMap();
  }

  /**
   * Loads up a Map of document types to supporting IStepAnalyzer(s)
   */
  protected void loadAnalyzerTypeMap() {
    analyzerTypeMap = new HashMap<Class<? extends BaseStepMeta>, Set<IStepAnalyzer>>();
    if ( stepAnalyzers != null ) {
      for ( IStepAnalyzer analyzer : stepAnalyzers ) {
        Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
        analyzer.setMetaverseBuilder( metaverseBuilder );
        if ( types != null ) {
          for ( Class<? extends BaseStepMeta> type : types ) {
            Set<IStepAnalyzer> analyzerSet = null;
            if ( analyzerTypeMap.containsKey( type ) ) {
              // we already have someone that handles this type, add to the Set
              analyzerSet = analyzerTypeMap.get( type );
            } else {
              // no one else (yet) handles this type, add it in
              analyzerSet = new HashSet<IStepAnalyzer>();
            }
            analyzerSet.add( analyzer );
            analyzerTypeMap.put( type, analyzerSet );
          }
        }
      }
    }
  }
}

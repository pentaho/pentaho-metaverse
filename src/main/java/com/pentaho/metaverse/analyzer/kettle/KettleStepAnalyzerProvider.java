package com.pentaho.metaverse.analyzer.kettle;

import org.pentaho.di.trans.step.BaseStepMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by mburgess on 7/29/14.
 */
public class KettleStepAnalyzerProvider extends BaseKettleMetaverseComponent implements IKettleStepAnalyzerProvider {

  /**
   * The step analyzers.
   */
  private Set<IStepAnalyzer> stepAnalyzers = new HashSet<IStepAnalyzer>();

  /**
   * The analyzer type map associates step meta classes with analyzers for those classes
   */
  private Map<Class<? extends BaseStepMeta>, HashSet<IStepAnalyzer>> analyzerTypeMap =
      new HashMap<Class<? extends BaseStepMeta>, HashSet<IStepAnalyzer>>();

  /**
   * @return
   */
  @Override public Set<IStepAnalyzer> getAnalyzers() {
    return stepAnalyzers;
  }

  /**
   * @param types
   * @return
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

  public void setStepAnalyzers( Set<IStepAnalyzer> analyzers ) {
    stepAnalyzers = analyzers;
    loadAnalyzerTypeMap();
  }

  /**
   * Loads up a Map of document types to supporting IAnalyzer<BaseStepMeta>(s)
   */
  protected void loadAnalyzerTypeMap() {
    analyzerTypeMap = new HashMap<Class<? extends BaseStepMeta>, HashSet<IStepAnalyzer>>();
    for ( IStepAnalyzer analyzer : stepAnalyzers ) {
      Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
      analyzer.setMetaverseBuilder( metaverseBuilder );
      if ( types != null ) {
        for ( Class<? extends BaseStepMeta> type : types ) {
          HashSet<IStepAnalyzer> analyzerSet = null;
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

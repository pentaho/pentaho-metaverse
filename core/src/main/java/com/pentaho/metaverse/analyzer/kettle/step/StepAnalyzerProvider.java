package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.metaverse.analyzer.kettle.BaseKettleMetaverseComponent;
import org.pentaho.di.trans.step.BaseStepMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The KettleStepAnalyzerProvider maintains a collection of analyzer objects capable of analyzing various PDI step
 */
public class StepAnalyzerProvider extends BaseKettleMetaverseComponent implements IStepAnalyzerProvider {

  /**
   * The set of step analyzers.
   */
  protected List<IStepAnalyzer> stepAnalyzers = new ArrayList<IStepAnalyzer>();

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
  public List<IStepAnalyzer> getAnalyzers() {
    return stepAnalyzers;
  }

  /**
   * Returns the set of analyzers for step with the specified classes
   *
   * @param types a set of classes corresponding to step for which to retrieve the analyzers
   * @return a set of analyzers that can process the specified step
   */
  @Override public List<IStepAnalyzer> getAnalyzers( Collection<Class<?>> types ) {
    List<IStepAnalyzer> stepAnalyzers = getAnalyzers();
    if ( types != null ) {
      final Set<IStepAnalyzer> specificStepAnalyzers = new HashSet<IStepAnalyzer>();
      for ( Class<?> clazz : types ) {
        if ( analyzerTypeMap.containsKey( clazz ) ) {
          specificStepAnalyzers.addAll( analyzerTypeMap.get( clazz ) );
        }
      }
      stepAnalyzers = new ArrayList<IStepAnalyzer>( specificStepAnalyzers );
    }
    return stepAnalyzers;
  }

  /**
   * Sets the collection of step analyzers used to analyze PDI step
   * @param analyzers
   */
  public void setStepAnalyzers( List<IStepAnalyzer> analyzers ) {
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
        addAnalyzer( analyzer );
      }
    }
  }

  @Override
  public void addAnalyzer( IStepAnalyzer analyzer ) {
    if ( !stepAnalyzers.contains( analyzer ) ) {
      stepAnalyzers.add( analyzer );
    }
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

  @Override
  public void removeAnalyzer( IStepAnalyzer analyzer ) {
    if ( stepAnalyzers.contains( analyzer ) ) {
      stepAnalyzers.remove( analyzer );
    }
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    if ( types != null ) {
      for ( Class<? extends BaseStepMeta> type : types ) {
        Set<IStepAnalyzer> analyzerSet = null;
        if ( analyzerTypeMap.containsKey( type ) ) {
          // we have someone that handles this type, remove it from the set
          analyzerSet = analyzerTypeMap.get( type );
          analyzerSet.remove( analyzer );
          if ( analyzerSet.size() == 0 ) {
            analyzerTypeMap.remove( type );
          }
        }
      }
    }
  }

}

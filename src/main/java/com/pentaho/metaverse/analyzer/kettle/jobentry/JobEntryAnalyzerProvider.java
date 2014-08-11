package com.pentaho.metaverse.analyzer.kettle.jobentry;

import com.pentaho.metaverse.analyzer.kettle.BaseKettleMetaverseComponent;
import org.pentaho.di.job.entry.JobEntryInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The KettleStepAnalyzerProvider maintains a collection of analyzer objects capable of analyzing various PDI step
 */
public class JobEntryAnalyzerProvider extends BaseKettleMetaverseComponent implements IJobEntryAnalyzerProvider {

  /**
   * The set of step analyzers.
   */
  protected Set<IJobEntryAnalyzer> jobEntryAnalyzers = new HashSet<IJobEntryAnalyzer>();

  /**
   * The analyzer type map associates step meta classes with analyzers for those classes
   */
  protected Map<Class<? extends JobEntryInterface>, Set<IJobEntryAnalyzer>> analyzerTypeMap =
      new HashMap<Class<? extends JobEntryInterface>, Set<IJobEntryAnalyzer>>();

  /**
   * Returns all registered step analyzers
   *
   * @return a set of step analyzers
   */
  @Override
  public Set<IJobEntryAnalyzer> getAnalyzers() {
    return jobEntryAnalyzers;
  }

  /**
   * Returns the set of analyzers for step with the specified classes
   *
   * @param types a set of classes corresponding to step for which to retrieve the analyzers
   * @return a set of analyzers that can process the specified step
   */
  @Override public Set<IJobEntryAnalyzer> getAnalyzers( Set<Class<?>> types ) {
    Set<IJobEntryAnalyzer> stepAnalyzers = getAnalyzers();
    if ( types != null ) {
      final Set<IJobEntryAnalyzer> specificStepAnalyzers = new HashSet<IJobEntryAnalyzer>();
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
   *
   * @param analyzers
   */
  public void setJobEntryAnalyzers( Set<IJobEntryAnalyzer> analyzers ) {
    jobEntryAnalyzers = analyzers;
    loadAnalyzerTypeMap();
  }

  /**
   * Loads up a Map of document types to supporting IJobEntryAnalyzer(s)
   */
  protected void loadAnalyzerTypeMap() {
    analyzerTypeMap = new HashMap<Class<? extends JobEntryInterface>, Set<IJobEntryAnalyzer>>();
    if ( jobEntryAnalyzers != null ) {
      for ( IJobEntryAnalyzer analyzer : jobEntryAnalyzers ) {
        Set<Class<? extends JobEntryInterface>> types = analyzer.getSupportedEntries();
        analyzer.setMetaverseBuilder( metaverseBuilder );
        if ( types != null ) {
          for ( Class<? extends JobEntryInterface> type : types ) {
            Set<IJobEntryAnalyzer> analyzerSet = null;
            if ( analyzerTypeMap.containsKey( type ) ) {
              // we already have someone that handles this type, add to the Set
              analyzerSet = analyzerTypeMap.get( type );
            } else {
              // no one else (yet) handles this type, add it in
              analyzerSet = new HashSet<IJobEntryAnalyzer>();
            }
            analyzerSet.add( analyzer );
            analyzerTypeMap.put( type, analyzerSet );
          }
        }
      }
    }
  }
}

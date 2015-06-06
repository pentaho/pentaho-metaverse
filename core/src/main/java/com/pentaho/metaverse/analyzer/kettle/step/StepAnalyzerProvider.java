/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.metaverse.api.analyzer.kettle.BaseKettleMetaverseComponent;
import com.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzer;
import com.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzerProvider;
import org.pentaho.di.trans.step.BaseStepMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * StepAnalyzerProvider maintains a collection of analyzer objects capable of analyzing various PDI steps
 */
public class StepAnalyzerProvider extends BaseKettleMetaverseComponent implements IStepAnalyzerProvider {

  /**
   * The set of step analyzers.
   */
  protected List<IStepAnalyzer> stepAnalyzers = new ArrayList<>();

  /**
   * The analyzer type map associates step meta classes with analyzers for those classes
   */
  protected Map<Class<? extends BaseStepMeta>, Set<IStepAnalyzer>> analyzerTypeMap = new HashMap<>();

  /**
   * Returns all registered step analyzers
   *
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
  @Override
  public List<IStepAnalyzer> getAnalyzers( Collection<Class<?>> types ) {
    List<IStepAnalyzer> stepAnalyzers = getAnalyzers();
    if ( types != null ) {
      final Set<IStepAnalyzer> specificStepAnalyzers = new HashSet<>();
      for ( Class<?> clazz : types ) {
        if ( analyzerTypeMap.containsKey( clazz ) ) {
          specificStepAnalyzers.addAll( analyzerTypeMap.get( clazz ) );
        }
      }
      stepAnalyzers = new ArrayList<>( specificStepAnalyzers );
    }
    return stepAnalyzers;
  }

  /**
   * Sets the list of step analyzers used to analyze PDI step
   *
   * @param analyzers the list of step analyzers for this object to provide
   */
  public void setStepAnalyzers( List<IStepAnalyzer> analyzers ) {
    stepAnalyzers = analyzers;
    loadAnalyzerTypeMap();
  }

  /**
   * Loads up a Map of document types to supporting IStepAnalyzer(s)
   */
  protected void loadAnalyzerTypeMap() {
    analyzerTypeMap = new HashMap<>();
    if ( stepAnalyzers != null ) {
      for ( IStepAnalyzer analyzer : stepAnalyzers ) {
        addAnalyzer( analyzer );
      }
    }
  }

  @Override
  public void addAnalyzer( IStepAnalyzer analyzer ) {
    if ( analyzer != null ) {
      if ( !stepAnalyzers.contains( analyzer ) ) {
        stepAnalyzers.add( analyzer );
      }
      Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
      analyzer.setMetaverseBuilder( metaverseBuilder );
      if ( types != null ) {
        for ( Class<? extends BaseStepMeta> type : types ) {
          Set<IStepAnalyzer> analyzerSet;
          if ( analyzerTypeMap.containsKey( type ) ) {
            // we already have someone that handles this type, add to the Set
            analyzerSet = analyzerTypeMap.get( type );
          } else {
            // no one else (yet) handles this type, add it in
            analyzerSet = new HashSet<>();
          }
          analyzerSet.add( analyzer );
          analyzerTypeMap.put( type, analyzerSet );
        }
      }
    }
  }

  @Override
  public void removeAnalyzer( IStepAnalyzer analyzer ) {
    if ( analyzer != null ) {
      if ( stepAnalyzers != null && stepAnalyzers.contains( analyzer ) ) {
        try {
          stepAnalyzers.remove( analyzer );
        } catch ( UnsupportedOperationException e ) {
          // reference-list doesn't support remove, just ignore
        }
      }

      Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
      if ( types != null ) {
        for ( Class<? extends BaseStepMeta> type : types ) {
          Set<IStepAnalyzer> analyzerSet;
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
}

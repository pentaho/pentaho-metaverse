/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.analyzer.kettle.step;

import com.google.common.annotations.VisibleForTesting;
import org.jaxen.expr.Step;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.api.analyzer.kettle.BaseKettleMetaverseComponent;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzerProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * StepAnalyzerProvider maintains a collection of analyzer objects capable of analyzing various PDI steps
 */
public class StepAnalyzerProvider extends BaseKettleMetaverseComponent implements IStepAnalyzerProvider {

  private static StepAnalyzerProvider instance;

  @VisibleForTesting
  StepAnalyzerProvider() {
  }

  public static StepAnalyzerProvider getInstance() {
    if ( null == instance ) {
      instance = new StepAnalyzerProvider();
    }
    return instance;
  }

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
    if ( null == stepAnalyzers || stepAnalyzers.isEmpty() ) {
      // could be the first time this has been invoked after startup; see who registered
      // this obviously does not support dynamically adding/removing analyzers at runtime
      stepAnalyzers = Collections.synchronizedList( new ArrayList<>() );
      stepAnalyzers.addAll( PentahoSystem.getAll( IStepAnalyzer.class ) );
      loadAnalyzerTypeMap();
    }
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
    if ( analyzers == null ) {
      this.stepAnalyzers = null;
    } else {
      if ( this.stepAnalyzers == null ) {
        this.stepAnalyzers = new ArrayList();
      }
      for ( final IStepAnalyzer analyzer : analyzers ) {
        if ( !stepAnalyzers.contains( analyzer ) ) {
          stepAnalyzers.add( analyzer );
        }
      }
      loadAnalyzerTypeMap();
    }
  }

  public void setClonableStepAnalyzers( List<IStepAnalyzer> analyzers ) {
    setStepAnalyzers( analyzers );
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

  public void addClonableAnalyzer( IClonableStepAnalyzer analyzer ) {
    addAnalyzer( analyzer );
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

  public void removeClonableAnalyzer( IClonableStepAnalyzer analyzer ) {
    removeAnalyzer( analyzer );
  }
}

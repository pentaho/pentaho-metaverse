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

package org.pentaho.metaverse.analyzer.kettle.jobentry;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.metaverse.api.analyzer.kettle.BaseKettleMetaverseComponent;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IClonableJobEntryAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryAnalyzerProvider;

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
public class JobEntryAnalyzerProvider extends BaseKettleMetaverseComponent implements IJobEntryAnalyzerProvider {

  private static JobEntryAnalyzerProvider instance;

  @VisibleForTesting
  JobEntryAnalyzerProvider() {
  }
  public static JobEntryAnalyzerProvider getInstance() {
    if ( null == instance ) {
      instance = new JobEntryAnalyzerProvider();
    }
    return instance;
  }
  /**
   * The set of step analyzers.
   */
  protected List<IJobEntryAnalyzer> jobEntryAnalyzers = new ArrayList<IJobEntryAnalyzer>();

  /**
   * The analyzer type map associates step meta classes with analyzers for those classes
   */
  protected Map<Class<? extends JobEntryInterface>, Set<IJobEntryAnalyzer>> analyzerTypeMap =
      new HashMap<Class<? extends JobEntryInterface>, Set<IJobEntryAnalyzer>>();

  /**
   * Returns all registered step analyzers
   *
   * @return a List of step analyzers
   */
  @Override
  public List<IJobEntryAnalyzer> getAnalyzers() {
    return jobEntryAnalyzers;
  }

  /**
   * Returns the set of analyzers for step with the specified classes
   *
   * @param types a set of classes corresponding to step for which to retrieve the analyzers
   * @return a set of analyzers that can process the specified step
   */
  @Override public List<IJobEntryAnalyzer> getAnalyzers( Collection<Class<?>> types ) {
    List<IJobEntryAnalyzer> jobEntryAnalyzers = getAnalyzers();
    if ( types != null ) {
      final Set<IJobEntryAnalyzer> specificStepAnalyzers = new HashSet<IJobEntryAnalyzer>();
      for ( Class<?> clazz : types ) {
        if ( analyzerTypeMap.containsKey( clazz ) ) {
          specificStepAnalyzers.addAll( analyzerTypeMap.get( clazz ) );
        }
      }
      jobEntryAnalyzers = new ArrayList<IJobEntryAnalyzer>( specificStepAnalyzers );
    }
    return jobEntryAnalyzers;
  }

  /**
   * Sets the collection of step analyzers used to analyze PDI step
   *
   * @param analyzers
   */
  public void setJobEntryAnalyzers( List<IJobEntryAnalyzer> analyzers ) {
    if ( analyzers == null ) {
      this.jobEntryAnalyzers = null;
    } else {
      if ( this.jobEntryAnalyzers == null ) {
        this.jobEntryAnalyzers = new ArrayList();
      }
      for ( final IJobEntryAnalyzer analyzer : analyzers ) {
        if ( !jobEntryAnalyzers.contains( analyzer ) ) {
          jobEntryAnalyzers.add( analyzer );
        }
      }
      loadAnalyzerTypeMap();
    }
  }

  public void setClonableJobEntryAnalyzers( List<IJobEntryAnalyzer> analyzers ) {
    setJobEntryAnalyzers( analyzers );
  }

  /**
   * Loads up a Map of document types to supporting IJobEntryAnalyzer(s)
   */
  protected void loadAnalyzerTypeMap() {
    analyzerTypeMap = new HashMap<Class<? extends JobEntryInterface>, Set<IJobEntryAnalyzer>>();
    if ( jobEntryAnalyzers != null ) {
      for ( IJobEntryAnalyzer analyzer : jobEntryAnalyzers ) {
        addAnalyzer( analyzer );
      }
    }
  }

  @Override
  public void addAnalyzer( IJobEntryAnalyzer analyzer ) {
    if ( !jobEntryAnalyzers.contains( analyzer ) ) {
      jobEntryAnalyzers.add( analyzer );
    }
    Set<Class<? extends JobEntryInterface>> types = analyzer.getSupportedEntries();
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

  public void addClonableAnalyzer( IClonableJobEntryAnalyzer analyzer ) {
    addAnalyzer( analyzer );
  }

  @Override
  public void removeAnalyzer( IJobEntryAnalyzer analyzer ) {
    if ( analyzer != null ) {
      if ( jobEntryAnalyzers.contains( analyzer ) ) {
        try {
          jobEntryAnalyzers.remove( analyzer );
        } catch ( UnsupportedOperationException e ) {
          // reference-list doesn't support remove
        }
      }
      Set<Class<? extends JobEntryInterface>> types = analyzer.getSupportedEntries();
      if ( types != null ) {
        for ( Class<? extends JobEntryInterface> type : types ) {
          Set<IJobEntryAnalyzer> analyzerSet = null;
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

  public void removeClonableAnalyzer( IClonableJobEntryAnalyzer analyzer ) {
    removeAnalyzer( analyzer );
  }

}

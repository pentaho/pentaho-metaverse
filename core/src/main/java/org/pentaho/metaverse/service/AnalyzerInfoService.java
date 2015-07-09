/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.service;

import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryAnalyzerProvider;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzerProvider;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Path( "/info" )
public class AnalyzerInfoService {

  private IStepAnalyzerProvider stepAnalyzerProvider;
  private IJobEntryAnalyzerProvider jobEntryAnalyzerProvider;

  public IStepAnalyzerProvider getStepAnalyzerProvider() {
    return stepAnalyzerProvider;
  }

  public void setStepAnalyzerProvider( IStepAnalyzerProvider stepAnalyzerProvider ) {
    this.stepAnalyzerProvider = stepAnalyzerProvider;
  }

  public IJobEntryAnalyzerProvider getJobEntryAnalyzerProvider() {
    return jobEntryAnalyzerProvider;
  }

  public void setJobEntryAnalyzerProvider( IJobEntryAnalyzerProvider jobEntryAnalyzerProvider ) {
    this.jobEntryAnalyzerProvider = jobEntryAnalyzerProvider;
  }

  @GET
  @Path( "/steps" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response getSupportedSteps() {
    List<AnalyzerInfo> analyzers = new ArrayList<>();
    for ( IStepAnalyzer analyzer : getStepAnalyzerProvider().getAnalyzers() ) {
      Set<Class<? extends BaseStepMeta>> supportedSteps = analyzer.getSupportedSteps();
      for ( Class<? extends BaseStepMeta> supportedStep : supportedSteps ) {
        AnalyzerInfo info = new AnalyzerInfo( supportedStep.getSimpleName() );
        analyzers.add( info );
      }
    }
    Collections.sort( analyzers, new AnalyzerInfoComparator() );
    return Response.ok( analyzers ).build();
  }

  @GET
  @Path( "/entries" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response getSupportedJobEntries() {
    List<AnalyzerInfo> analyzers = new ArrayList<>();
    for ( IJobEntryAnalyzer analyzer : getJobEntryAnalyzerProvider().getAnalyzers() ) {
      Set<Class<? extends JobEntryInterface>> supportedEntries = analyzer.getSupportedEntries();
      for ( Class<? extends JobEntryInterface> supportedEntry : supportedEntries ) {
        AnalyzerInfo info = new AnalyzerInfo( supportedEntry.getSimpleName() );
        analyzers.add( info );
      }
    }
    Collections.sort( analyzers, new AnalyzerInfoComparator() );
    return Response.ok( analyzers ).build();
  }

  class AnalyzerInfoComparator implements Comparator<AnalyzerInfo> {
    @Override
    public int compare( AnalyzerInfo left, AnalyzerInfo right ) {
      return left.getMeta().compareTo( right.getMeta() );
    }
  }

}

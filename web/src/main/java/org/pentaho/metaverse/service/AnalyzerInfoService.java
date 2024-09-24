/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
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

/**
 * Provides information about the lineage analyzers active in the system
 */
@Path( "/info" )
public class AnalyzerInfoService {

  public static final int OK = 200;
  public static final int SERVER_ERROR = 500;

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

  /**
   * Gets a list of all implementations of {@link BaseStepMeta} with a custom {@link IStepAnalyzer}
   * that produces lineage. Any step not found in this list will fall back to using
   * {@link org.pentaho.metaverse.analyzer.kettle.step.GenericStepMetaAnalyzer}.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho-di/osgi/cxf/lineage/info/steps
   * </p>
   *
   * @return List of {@link AnalyzerInfo}
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.js">
   *      [ { meta: "CalculatorMeta" }, { meta: "CsvInputMeta" } ]
   *    </pre>
   */
  @GET
  @Path( "/steps" )
  @Produces( { MediaType.APPLICATION_JSON } )
  @StatusCodes( {
    @ResponseCode( code = OK, condition = "Successfully listed the supported steps" ),
    @ResponseCode( code = SERVER_ERROR, condition = "Server Error." )
  } )
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

  /**
   * Gets a list of all implementations of {@link JobEntryInterface} with a custom {@link IJobEntryAnalyzer}
   * that produces lineage. Any step not found in this list will fall back to using
   * {@link org.pentaho.metaverse.analyzer.kettle.jobentry.GenericJobEntryMetaAnalyzer}.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho-di/osgi/cxf/lineage/info/entries
   * </p>
   *
   * @return List of {@link AnalyzerInfo}
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.js">
   *      [ { meta: "JobEntryTrans" } ]
   *    </pre>
   */
  @GET
  @Path( "/entries" )
  @Produces( { MediaType.APPLICATION_JSON } )
  @StatusCodes( {
    @ResponseCode( code = OK, condition = "Successfully listed the supported job entries" ),
    @ResponseCode( code = SERVER_ERROR, condition = "Server Error." )
  } )
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

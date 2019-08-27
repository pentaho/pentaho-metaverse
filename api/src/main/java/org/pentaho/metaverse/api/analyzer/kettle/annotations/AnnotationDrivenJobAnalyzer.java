/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.metaverse.api.analyzer.kettle.annotations;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.JobEntryAnalyzer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotationDrivenJobAnalyzer  extends JobEntryAnalyzer<JobEntryInterface> {

  private transient JobEntryInterface jobEntry;

  public AnnotationDrivenJobAnalyzer( JobEntryInterface jobEntry ) {
    this.jobEntry = jobEntry;
  }

  @Override protected void customAnalyze( JobEntryInterface entry, IMetaverseNode rootNode )
    throws MetaverseAnalyzerException {
    List<BaseStepMeta> baseStepMetas = Arrays.stream( entry.getClass().getMethods() )
      .filter( m -> m.isAnnotationPresent( Metaverse.InternalStepMeta.class ) )
      .map( method -> getBaseStepMeta( entry, method ) )
      .collect( Collectors.toList() );
    for ( BaseStepMeta baseStepMeta : baseStepMetas ) {
      analyzeInternalStepMeta( rootNode, baseStepMeta );
    }
  }

  private BaseStepMeta getBaseStepMeta( JobEntryInterface entry, Method method ) {
    try {
      return (BaseStepMeta) method.invoke( entry );
    } catch ( Exception e ) {
      throw new IllegalStateException( "InternalStepMeta annotation only allowed on BaseStepMeta methods" );
    }
  }

  private void analyzeInternalStepMeta( IMetaverseNode rootNode, BaseStepMeta baseStepMeta ) throws MetaverseAnalyzerException {
    AnnotationDrivenStepMetaAnalyzer stepAnalyzer = createStepAnalyzer( baseStepMeta );
    stepAnalyzer.setMetaverseBuilder( metaverseBuilder );
    stepAnalyzer.setDocumentAnalyzer( documentAnalyzer );
    stepAnalyzer.setDocumentDescriptor( documentDescriptor );
    stepAnalyzer.setDocumentPath( documentPath );
    stepAnalyzer.setDescriptor( descriptor );
    stepAnalyzer.customAnalyze( baseStepMeta, rootNode );
  }

  @VisibleForTesting
  AnnotationDrivenStepMetaAnalyzer createStepAnalyzer( BaseStepMeta baseStepMeta ) {
    return new AnnotationDrivenStepMetaAnalyzer( baseStepMeta, jobEntry.getParentJobMeta() );
  }

  @Override public Set<Class<? extends JobEntryInterface>> getSupportedEntries() {
    return Collections.singleton( jobEntry.getClass() );
  }
}

/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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

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


package org.pentaho.metaverse.analyzer.kettle.step.rowstoresult;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.rowstoresult.RowsToResultMeta;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by rfellows on 4/3/15.
 */
public class RowsToResultStepAnalyzer extends StepAnalyzer<RowsToResultMeta> {

  @Override
  protected Set<StepField> getUsedFields( RowsToResultMeta meta ) {
    return null;
  }

  @Override
  protected void customAnalyze( RowsToResultMeta meta, IMetaverseNode rootNode )
    throws MetaverseAnalyzerException {
    // nothing custom
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    supportedSteps.add( RowsToResultMeta.class );
    return supportedSteps;
  }

  @Override
  public IClonableStepAnalyzer newInstance() {
    return new RowsToResultStepAnalyzer();
  }
}

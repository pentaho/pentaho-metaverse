/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.metaverse.analyzer.kettle.step;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;

import java.util.Set;

/**
 * KettleGenericStepMetaAnalyzer provides a default implementation for analyzing PDI step
 * to gather metadata for the metaverse.
 */
public class GenericStepMetaAnalyzer extends StepAnalyzer<BaseStepMeta> {

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return null;
  }

  @Override
  protected Set<StepField> getUsedFields( BaseStepMeta meta ) {
    return null;
  }

  @Override
  protected void customAnalyze( BaseStepMeta meta, IMetaverseNode rootNode ) {
    // nothing custom to do here since it's the catch-all step analyzer
  }
}

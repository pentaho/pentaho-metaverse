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

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

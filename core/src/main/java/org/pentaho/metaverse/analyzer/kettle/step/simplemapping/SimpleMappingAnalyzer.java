/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.simplemapping;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.simplemapping.SimpleMappingMeta;
import org.pentaho.metaverse.analyzer.kettle.step.mapping.BaseMappingAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;

import java.util.HashSet;
import java.util.Set;

public class SimpleMappingAnalyzer extends BaseMappingAnalyzer<SimpleMappingMeta> {

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> supported = new HashSet<>();
    supported.add( SimpleMappingMeta.class );
    return supported;
  }

  @Override
  public IClonableStepAnalyzer newInstance() {
    return new SimpleMappingAnalyzer();
  }

}

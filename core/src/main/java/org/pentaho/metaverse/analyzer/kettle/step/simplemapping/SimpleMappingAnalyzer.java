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

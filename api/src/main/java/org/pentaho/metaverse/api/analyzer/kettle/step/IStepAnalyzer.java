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


package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.api.IAnalyzer;
import org.pentaho.metaverse.api.IMetaverseNode;

import java.util.Set;

/**
 * The IStepAnalyzer interface is a helper interface for classes that analyze PDI step.
 */
public interface IStepAnalyzer<T extends BaseStepMeta> extends IAnalyzer<IMetaverseNode, T> {

  /**
   * Returns a set of BaseStepMeta classes representing the steps that can be analyzed by this step analyzer.
   *
   * @return a Set of Classes (each of which extend BaseStepMeta)
   */
  Set<Class<? extends BaseStepMeta>> getSupportedSteps();
}

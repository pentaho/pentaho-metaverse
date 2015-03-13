package com.pentaho.metaverse.analyzer.kettle.step;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.platform.api.metaverse.IAnalyzer;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

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

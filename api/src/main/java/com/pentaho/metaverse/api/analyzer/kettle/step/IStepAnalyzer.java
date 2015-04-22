package com.pentaho.metaverse.api.analyzer.kettle.step;

import org.pentaho.di.trans.step.BaseStepMeta;
import com.pentaho.metaverse.api.IAnalyzer;
import com.pentaho.metaverse.api.IMetaverseNode;

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

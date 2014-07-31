package com.pentaho.metaverse.analyzer.kettle;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.platform.api.metaverse.IAnalyzer;

import java.util.Set;

/**
 * The IStepAnalyzer interface is a helper interface for classes that analyze PDI steps.
 */
public interface IStepAnalyzer<T extends BaseStepMeta> extends IAnalyzer<T> {

  Set<Class<? extends BaseStepMeta>> getSupportedSteps();
}

package com.pentaho.metaverse.analyzer.kettle.step;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.platform.api.metaverse.IAnalysisContext;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

/**
 * The IRowAnalyzer interface adds support for data-driven (i.e. row-level) analysis of transformation steps
 */
public interface IRowAnalyzer<T extends BaseStepMeta> {

  void analyzeRow(
      T stepMeta,
      INamespace stepNamespace,
      IMetaverseNode stepNode,
      IAnalysisContext context,
      RowMetaInterface rowMeta,
      Object[] rowData ) throws MetaverseAnalyzerException;

}

package com.pentaho.metaverse.analyzer.kettle.step;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.platform.api.metaverse.IAnalysisContext;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

/**
 * Created by mburgess on 9/4/14.
 */
public class StepAnalyzerRowListener<T extends BaseStepMeta> implements RowListener {

  protected T stepMeta;

  protected INamespace namespace;

  protected IMetaverseNode stepNode;

  protected IAnalysisContext context;

  protected IRowAnalyzer<T> rowAnalyzer;

  public StepAnalyzerRowListener(
      T stepMeta,
      INamespace namespace,
      IMetaverseNode stepNode,
      IAnalysisContext context,
      IRowAnalyzer<T> rowAnalyzer ) {

    this.stepMeta = stepMeta;
    this.namespace = namespace;
    this.stepNode = stepNode;
    this.context = context;
    this.rowAnalyzer = rowAnalyzer;

  }

  /**
   * This method is called when a row is read from another step
   *
   * @param rowMeta the metadata of the row
   * @param rowData the data of the row
   * @throws org.pentaho.di.core.exception.KettleStepException an exception that can be thrown to hard stop the step
   */
  @Override
  public void rowReadEvent( RowMetaInterface rowMeta, Object[] rowData ) throws KettleStepException {
    try {
      rowAnalyzer.analyzeRow( stepMeta, namespace, stepNode, context, rowMeta, rowData );
    } catch ( MetaverseAnalyzerException mae ) {
      throw new KettleStepException( mae );
    }
  }

  /**
   * This method is called when a row is written to another step (even if there is no next step)
   *
   * @param rowMeta the metadata of the row
   * @param row     the data of the row
   * @throws org.pentaho.di.core.exception.KettleStepException an exception that can be thrown to hard stop the step
   */
  @Override
  public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {

  }

  /**
   * This method is called when the error handling of a row is writing a row to the error stream.
   *
   * @param rowMeta the metadata of the row
   * @param row     the data of the row
   * @throws org.pentaho.di.core.exception.KettleStepException an exception that can be thrown to hard stop the step
   */
  @Override
  public void errorRowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {

  }
}

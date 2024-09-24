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

package org.pentaho.metaverse.analyzer.kettle;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.api.IAnalyzer;
import org.pentaho.metaverse.api.IMetaverseNode;

/**
 * A simple wrapper for the {@link IAnalyzer}, {@link BaseStepMeta} and {@link IMetaverseNode}.
 */
public class AnalyzerHolder {

  private IAnalyzer analyzer;
  private BaseStepMeta meta;
  private IMetaverseNode node;

  public AnalyzerHolder( final IAnalyzer analyzer, final BaseStepMeta meta, final IMetaverseNode node ) {
    this.analyzer = analyzer;
    this.meta = meta;
    this.node = node;
  }

  public IAnalyzer getAnalyzer() {
    return analyzer;
  }

  public BaseStepMeta getMeta() {
    return meta;
  }

  public IMetaverseNode getNode() {
    return node;
  }
}

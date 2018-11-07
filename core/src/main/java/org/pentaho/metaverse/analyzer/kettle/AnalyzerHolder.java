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

/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 *
 */

package com.pentaho.metaverse.sample;

import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.StepField;
import com.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

import java.util.HashSet;
import java.util.Set;

public class DummyStepAnalyzer extends StepAnalyzer<DummyTransMeta> {
  @Override
  protected Set<StepField> getUsedFields( DummyTransMeta meta ) {
    // no incoming fields are used by the Dummy step
    return null;
  }

  @Override
  protected void customAnalyze( DummyTransMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    // add any custom properties or relationships here
    rootNode.setProperty( "do_nothing", true );
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<>();
    supportedSteps.add( DummyTransMeta.class );
    return supportedSteps;
  }
}

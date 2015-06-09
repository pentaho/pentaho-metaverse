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
 */

package org.pentaho.metaverse.analyzer.kettle.step.textfileinput;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * The TextFileInputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and
 * other metaverse entities
 */
public class TextFileInputStepAnalyzer extends ExternalResourceStepAnalyzer<TextFileInputMeta> {

  @Override
  protected Set<StepField> getUsedFields( TextFileInputMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();
    if ( meta.isAcceptingFilenames() && StringUtils.isNotEmpty( meta.getAcceptingStepName() ) ) {
      StepField stepField = new StepField( meta.getAcceptingStepName(), meta.getAcceptingField() );
      usedFields.add( stepField );
    }
    return usedFields;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( TextFileInputMeta.class );
      }
    };
  }

  @Override
  public IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException {
    return createFileNode( resource.getName(), descriptor );
  }

  @Override
  public String getResourceInputNodeType() {
    return DictionaryConst.NODE_TYPE_FILE_FIELD;
  }

  @Override
  public String getResourceOutputNodeType() {
    return null;
  }

  @Override
  public boolean isOutput() {
    return false;
  }

  @Override
  public boolean isInput() {
    return true;
  }
}

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

package org.pentaho.metaverse.analyzer.kettle.step.fileinput.text;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInput;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;

public class TextFileInputExternalResourceConsumer
  extends BaseStepExternalResourceConsumer<TextFileInput, TextFileInputMeta> {

  private static TextFileInputExternalResourceConsumer instance;

  @VisibleForTesting
  protected TextFileInputExternalResourceConsumer() {
  }

  public static TextFileInputExternalResourceConsumer getInstance() {
    if ( null == instance ) {
      instance = new TextFileInputExternalResourceConsumer();
    }
    return instance;
  }

  @Override
  public boolean isDataDriven( TextFileInputMeta meta ) {
    // We can safely assume that the StepMetaInterface object we get back is a TextFileInputMeta
    return meta.isAcceptingFilenames();
  }

  @Override
  public Class<TextFileInputMeta> getMetaClass() {
    return TextFileInputMeta.class;
  }
}

/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

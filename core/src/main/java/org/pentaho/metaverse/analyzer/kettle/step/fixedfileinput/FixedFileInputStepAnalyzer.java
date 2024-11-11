/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.step.fixedfileinput;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.fixedinput.FixedInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * The CsvInputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and other
 * metaverse entities
 */
public class FixedFileInputStepAnalyzer extends ExternalResourceStepAnalyzer<FixedInputMeta> {
  private Logger log = LoggerFactory.getLogger( FixedFileInputStepAnalyzer.class );

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( FixedInputMeta.class );
      }
    };
  }

  @Override public IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException {
    return createFileNode( resource.getName(), descriptor );
  }

  @Override public String getResourceInputNodeType() {
    return DictionaryConst.NODE_TYPE_FILE_FIELD;
  }

  @Override public String getResourceOutputNodeType() {
    return null;
  }

  @Override public boolean isOutput() {
    return false;
  }

  @Override public boolean isInput() {
    return true;
  }

  @Override protected Set<StepField> getUsedFields( FixedInputMeta meta ) {
    return null;
  }
  // used for unit testing
  protected void setObjectFactory( IMetaverseObjectFactory factory ) {
    this.metaverseObjectFactory = factory;
  }

  @Override protected IClonableStepAnalyzer newInstance() {
    return new FixedFileInputStepAnalyzer();
  }

  @Override public String toString() {
    return this.getClass().getName();
  }
}

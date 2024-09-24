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

package org.pentaho.metaverse.analyzer.kettle.step.fixedfileinput;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.fixedinput.FixedInput;
import org.pentaho.di.trans.steps.fixedinput.FixedInputMeta;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class FixedFileInputExternalResourceConsumer extends BaseStepExternalResourceConsumer<FixedInput, FixedInputMeta> {

  private static FixedFileInputExternalResourceConsumer instance;

  @VisibleForTesting
  protected FixedFileInputExternalResourceConsumer() {
  }

  public static FixedFileInputExternalResourceConsumer getInstance() {
    if ( null == instance ) {
      instance = new FixedFileInputExternalResourceConsumer();
    }
    return instance;
  }

  @Override
  public boolean isDataDriven( FixedInputMeta meta ) {
    // We can safely assume that the StepMetaInterface object we get back is a FixedInputMeta
    return false;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( FixedInputMeta meta, IAnalysisContext context ) {
    Collection<IExternalResourceInfo> resources = Collections.emptyList();

    // We only need to collect these resources if we're not data-driven and there are no used variables in the
    // metadata relating to external files.
    if ( !isDataDriven( meta ) ) {
      StepMeta parentStepMeta = meta.getParentStepMeta();
      if ( parentStepMeta != null ) {
        TransMeta parentTransMeta = parentStepMeta.getParentTransMeta();
        if ( parentTransMeta != null ) {
          String[] paths = new String[] {meta.getFilename()};
          if ( paths != null ) {
            resources = new ArrayList<IExternalResourceInfo>( paths.length );

            for ( String path : paths ) {
              if ( !Const.isEmpty( path ) ) {
                try {

                  IExternalResourceInfo resource = ExternalResourceInfoFactory
                    .createFileResource( KettleVFS.getFileObject( path ), true );
                  if ( resource != null ) {
                    resources.add( resource );
                  } else {
                    throw new KettleFileException( "Error getting file resource!" );
                  }
                } catch ( KettleFileException kfe ) {
                  // TODO throw or ignore?
                }
              }
            }
          }
        }
      }
    }
    return resources;
  }

  @Override
  public Class<FixedInputMeta> getMetaClass() {
    return FixedInputMeta.class;
  }
}

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


package org.pentaho.metaverse.analyzer.kettle.step.textfileoutput;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutput;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputData;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class TextFileOutputExternalResourceConsumer
  extends BaseStepExternalResourceConsumer<TextFileOutput, TextFileOutputMeta> {

  private static TextFileOutputExternalResourceConsumer instance;

  @VisibleForTesting
  protected TextFileOutputExternalResourceConsumer() {
  }

  public static TextFileOutputExternalResourceConsumer getInstance() {
    if ( null == instance ) {
      instance = new TextFileOutputExternalResourceConsumer();
    }
    return instance;
  }

  @Override
  public boolean isDataDriven( TextFileOutputMeta meta ) {
    // We can safely assume that the StepMetaInterface object we get back is a TextFileOutputMeta
    return meta.isFileNameInField();
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( TextFileOutputMeta meta, IAnalysisContext context ) {
    Collection<IExternalResourceInfo> resources = Collections.emptyList();

    // We only need to collect these resources if we're not data-driven and there are no used variables in the
    // metadata relating to external files.
    if ( !isDataDriven( meta ) /* TODO */ ) {
      StepMeta parentStepMeta = meta.getParentStepMeta();
      if ( parentStepMeta != null ) {
        TransMeta parentTransMeta = parentStepMeta.getParentTransMeta();
        if ( parentTransMeta != null ) {
          String[] paths = meta.getFiles( parentTransMeta );
          if ( paths != null ) {
            resources = new ArrayList<IExternalResourceInfo>( paths.length );

            for ( String path : paths ) {
              if ( !Const.isEmpty( path ) ) {
                try {

                  IExternalResourceInfo resource = ExternalResourceInfoFactory
                    .createFileResource( KettleVFS.getFileObject( path ), false );
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
  public Collection<IExternalResourceInfo> getResourcesFromRow(
    TextFileOutput textFileOutput, RowMetaInterface rowMeta, Object[] row ) {
    Collection<IExternalResourceInfo> resources = new LinkedList<IExternalResourceInfo>();
    // For some reason the step doesn't return the StepMetaInterface directly, so go around it
    TextFileOutputMeta meta = (TextFileOutputMeta) textFileOutput.getStepMeta().getStepMetaInterface();

    try {
      TextFileOutputData data = ( (TextFileOutputData) textFileOutput.getStepDataInterface() );
      String filename = rowMeta.getString( row, meta.getFileNameField(), meta.getFileName() );
      if ( null != data ) {
        // For some reason, the first call to process row doesn't have the data.fileName filled in, so
        // fall back to the filename field value, and then to the meta's filename
        filename = textFileOutput.buildFilename( Const.isEmpty( data.fileName ) ? filename : data.fileName, true );
      }

      if ( !Const.isEmpty( filename ) ) {
        FileObject fileObject = KettleVFS.getFileObject( filename );
        resources.add( ExternalResourceInfoFactory.createFileResource( fileObject, false ) );
      }
    } catch ( KettleException kve ) {
      // TODO throw exception or ignore?
    }

    return resources;
  }

  @Override
  public Class<TextFileOutputMeta> getMetaClass() {
    return TextFileOutputMeta.class;
  }
}

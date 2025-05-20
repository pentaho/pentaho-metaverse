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


package org.pentaho.metaverse.analyzer.kettle.step.textfileinput;

import com.cronutils.utils.VisibleForTesting;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInput;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.metaverse.analyzer.kettle.step.tableoutput.TableOutputExternalResourceConsumer;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

// This annotation is here to show that we know the referenced classes are deprecated, but we support them anyway (from
// a lineage perspective)
@SuppressWarnings( "deprecation" )
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
  public Collection<IExternalResourceInfo> getResourcesFromMeta( Bowl bowl, TextFileInputMeta meta,
    IAnalysisContext context ) {
    Collection<IExternalResourceInfo> resources = Collections.emptyList();

    // We only need to collect these resources if we're not data-driven and there are no used variables in the
    // metadata relating to external files.
    if ( !isDataDriven( meta ) /* TODO */ ) {
      StepMeta parentStepMeta = meta.getParentStepMeta();
      if ( parentStepMeta != null ) {
        TransMeta parentTransMeta = parentStepMeta.getParentTransMeta();
        if ( parentTransMeta != null ) {
          String[] paths = meta.getFilePaths( parentTransMeta );
          if ( paths != null ) {
            resources = new ArrayList<>( paths.length );

            for ( String path : paths ) {
              if ( !Const.isEmpty( path ) ) {
                try {

                  IExternalResourceInfo resource = ExternalResourceInfoFactory
                    .createFileResource( KettleVFS.getInstance( bowl ).getFileObject( path ), true );
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
    TextFileInput textFileInput, RowMetaInterface rowMeta, Object[] row ) {
    Collection<IExternalResourceInfo> resources = new LinkedList<>();
    // For some reason the step doesn't return the StepMetaInterface directly, so go around it
    TextFileInputMeta meta = (TextFileInputMeta) textFileInput.getStepMetaInterface();
    if ( meta == null ) {
      meta = (TextFileInputMeta) textFileInput.getStepMeta().getStepMetaInterface();
    }

    try {
      String filename = meta == null ? null : rowMeta.getString( row, meta.getAcceptingField(), null );
      if ( !Const.isEmpty( filename ) ) {
        FileObject fileObject = KettleVFS.getInstance( textFileInput.getTransMeta().getBowl() )
          .getFileObject( filename );
        resources.add( ExternalResourceInfoFactory.createFileResource( fileObject, true ) );
      }
    } catch ( KettleException kve ) {
      // TODO throw exception or ignore?
    }

    return resources;
  }

  @Override
  public Class<TextFileInputMeta> getMetaClass() {
    return TextFileInputMeta.class;
  }
}

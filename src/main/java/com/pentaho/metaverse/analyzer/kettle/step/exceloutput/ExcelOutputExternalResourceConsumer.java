/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.analyzer.kettle.step.exceloutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutput;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutputMeta;

import com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.step.BaseStepExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumer;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.impl.model.ExternalResourceInfoFactory;

@ExternalResourceConsumer( id = "ExcelOutputExternalResourceConsumer", name = "ExcelOutputExternalResourceConsumer" )
public class ExcelOutputExternalResourceConsumer extends BaseStepExternalResourceConsumer<ExcelOutput, ExcelOutputMeta> {

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( ExcelOutputMeta meta ) {
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

                  IExternalResourceInfo resource =
                      ExternalResourceInfoFactory.createFileResource( KettleVFS.getFileObject( path ), false );
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
  public Collection<IExternalResourceInfo> getResourcesFromRow( ExcelOutput excelOutput, RowMetaInterface rowMeta,
      Object[] row ) {
    Collection<IExternalResourceInfo> resources = new LinkedList<IExternalResourceInfo>();
    // For some reason the step doesn't return the StepMetaInterface directly, so go around it

    try {
      String filename = excelOutput.buildFilename();
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
  public Class<ExcelOutputMeta> getMetaClass() {
    return ExcelOutputMeta.class;
  }
}

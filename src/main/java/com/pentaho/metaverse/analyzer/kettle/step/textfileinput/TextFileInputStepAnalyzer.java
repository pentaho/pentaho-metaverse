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

package com.pentaho.metaverse.analyzer.kettle.step.textfileinput;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.BaseStepExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;
import org.pentaho.platform.api.metaverse.MetaverseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * The TextFileInputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and
 * other metaverse entities
 */
public class TextFileInputStepAnalyzer extends BaseStepAnalyzer<TextFileInputMeta> {
  private Logger log = LoggerFactory.getLogger( TextFileInputStepAnalyzer.class );

  @Override
  public IMetaverseNode analyze( IMetaverseComponentDescriptor descriptor, TextFileInputMeta textFileInputMeta )
    throws MetaverseAnalyzerException {

    // do the common analysis for all step
    IMetaverseNode node = super.analyze( descriptor, textFileInputMeta );

    // add the fields as nodes, add the links too
    TextFileInputField[] fields = textFileInputMeta.getInputFields();
    if ( fields != null ) {
      for ( TextFileInputField field : fields ) {
        String fieldName = field.getName();
        IMetaverseComponentDescriptor fileFieldDescriptor = new MetaverseComponentDescriptor(
          fieldName,
          DictionaryConst.NODE_TYPE_FILE_FIELD,
          descriptor.getNamespace(),
          descriptor.getContext() );
        IMetaverseNode fieldNode = createNodeFromDescriptor( fileFieldDescriptor );
        metaverseBuilder.addNode( fieldNode );

        // Get the stream field output from this step. It should've already been created when we called super.analyze()
        IMetaverseComponentDescriptor transFieldDescriptor = getStepFieldOriginDescriptor( descriptor, fieldName );
        IMetaverseNode outNode = createNodeFromDescriptor( transFieldDescriptor );

        metaverseBuilder.addLink( fieldNode, DictionaryConst.LINK_POPULATES, outNode );

        // add a link from the fileField to the text file input step node
        metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, fieldNode );
      }
    }

    if ( textFileInputMeta.isAcceptingFilenames() ) {
      String acceptingFieldName = textFileInputMeta.getAcceptingField();
      IMetaverseComponentDescriptor transFieldDescriptor = getPrevStepFieldOriginDescriptor( descriptor,
        acceptingFieldName );
      IMetaverseNode acceptingFieldNode = createNodeFromDescriptor( transFieldDescriptor );

      // add a link from the fileField to the text file input step node
      metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, acceptingFieldNode );
    } else {
      String[] fileNames = parentTransMeta.environmentSubstitute( textFileInputMeta.getFileName() );

      // add a link from the file(s) being read to the step
      for ( String fileName : fileNames ) {
        if ( !Const.isEmpty( fileName ) ) {
          try {
            // first add the node for the file
            IMetaverseNode textFileNode = createFileNode( fileName, descriptor );
            metaverseBuilder.addNode( textFileNode );

            metaverseBuilder.addLink( textFileNode, DictionaryConst.LINK_READBY, node );
          } catch ( MetaverseException e ) {
            log.error( e.getMessage(), e );
          }
        }
      }
    }

    return node;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( TextFileInputMeta.class );
      }
    };
  }

  @ExternalResourceConsumer(
    id = "TextFileInputExternalResourceConsumer",
    name = "TextFileInputExternalResourceConsumer"
  )
  public static class TextFileInputExternalResourceConsumer
    extends BaseStepExternalResourceConsumer<TextFileInputMeta> {

    @Override
    public boolean isDataDriven( TextFileInputMeta meta ) {
      // We can safely assume that the StepMetaInterface object we get back is a TextFileInputMeta
      return meta.isAcceptingFilenames();
    }

    @Override
    public Collection<IExternalResourceInfo> getResourcesFromMeta( TextFileInputMeta meta ) {
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
              resources = new ArrayList<IExternalResourceInfo>( paths.length );

              for ( String path : paths ) {
                if ( !Const.isEmpty( path ) ) {
                  try {

                    IExternalResourceInfo resource = getFileResource( KettleVFS.getFileObject( path ), true );
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
      TextFileInputMeta meta, RowMetaInterface rowMeta, Object[] row ) {
      Collection<IExternalResourceInfo> resources = new LinkedList<IExternalResourceInfo>();

      try {
        String filename = rowMeta.getString( row, meta.getAcceptingField(), null );
        if ( !Const.isEmpty( filename ) ) {
          FileObject fileObject = KettleVFS.getFileObject( filename );
          resources.add( getFileResource( fileObject, true ) );
        }
      } catch ( KettleException kve ) {
        // TODO throw exception or ignore?
      }

      return resources;
    }

    @Override
    public Class<TextFileInputMeta> getStepMetaClass() {
      return TextFileInputMeta.class;
    }
  }
}

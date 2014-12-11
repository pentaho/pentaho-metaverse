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

package com.pentaho.metaverse.analyzer.kettle.step.textfileoutput;

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
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
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
 * The TextFileOutputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the
 * fields operated on by Text File Output steps.
 */
public class TextFileOutputStepAnalyzer extends BaseStepAnalyzer<TextFileOutputMeta> {

  private Logger log = LoggerFactory.getLogger( TextFileOutputStepAnalyzer.class );

  @Override
  public IMetaverseNode analyze( IMetaverseComponentDescriptor descriptor, TextFileOutputMeta meta )
    throws MetaverseAnalyzerException {

    // do the common analysis...
    super.analyze( descriptor, meta );

    if ( !meta.isFileNameInField() ) {
      addStaticFileNodeAndLink( descriptor, meta );
    } else {
      addUsesLinkForStreamFileNameField( descriptor, meta );
    }
    // TODO: handle runtime file splitting for every X number of rows.
    // TODO: handle runtime "Accepts filename from field" - row level, dynamic file metadata

    addFieldNodesAndLinks( descriptor, meta );

    return rootNode;

  }

  protected void addStaticFileNodeAndLink( IMetaverseComponentDescriptor descriptor, TextFileOutputMeta meta ) {
    // get the file(s) that are being written to
    String[] files = meta.getFiles( meta.getParentStepMeta().getParentTransMeta() );

    // create node(s) form them
    for ( String file : files ) {
      if ( !Const.isEmpty( file ) ) {
        try {
          IMetaverseNode fileNode = createFileNode( file, descriptor );
          metaverseBuilder.addNode( fileNode );
          // add 'writesto' links to them from the rootNode
          metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_WRITESTO, fileNode );
        } catch ( MetaverseException e ) {
          log.error( e.getMessage(), e );
        }
      }
    }
  }

  protected void addUsesLinkForStreamFileNameField( IMetaverseComponentDescriptor descriptor,
                                                    TextFileOutputMeta meta ) {

    ValueMetaInterface filenameField = prevFields.searchValueMeta( meta.getFileNameField() );
    if ( filenameField != null ) {
      IMetaverseComponentDescriptor fileStreamFieldDescriptor = getStepFieldOriginDescriptor( descriptor,
        filenameField.getName() );
      IMetaverseNode fileStreamFieldNode = createNodeFromDescriptor( fileStreamFieldDescriptor );
      metaverseBuilder.addNode( fileStreamFieldNode );
      metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, fileStreamFieldNode );
    }
  }

  protected void addFieldNodesAndLinks( IMetaverseComponentDescriptor descriptor, TextFileOutputMeta meta ) {
    TextFileField[] outputFields = meta.getOutputFields();
    for ( TextFileField outputField : outputFields ) {
      String fieldName = outputField.getName();
      IMetaverseComponentDescriptor fileFieldDescriptor = new MetaverseComponentDescriptor(
        fieldName,
        DictionaryConst.NODE_TYPE_FILE_FIELD,
        descriptor,
        descriptor.getContext() );

      // create the file field nodes
      IMetaverseNode fieldNode = createNodeFromDescriptor( fileFieldDescriptor );
      metaverseBuilder.addNode( fieldNode );

      IMetaverseComponentDescriptor transFieldDescriptor = getStepFieldOriginDescriptor( descriptor, fieldName );
      IMetaverseNode transFieldNode = createNodeFromDescriptor( transFieldDescriptor );

      // add 'populates' links from the stream fields to them
      metaverseBuilder.addLink( transFieldNode, DictionaryConst.LINK_POPULATES, fieldNode );

      // add the links for "uses" stream fields
      metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, transFieldNode );
    }
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( TextFileOutputMeta.class );
      }
    };
  }

  @ExternalResourceConsumer(
    id = "TextFileOutputExternalResourceConsumer",
    name = "TextFileOutputExternalResourceConsumer"
  )
  public static class TextFileOutputExternalResourceConsumer
    extends BaseStepExternalResourceConsumer<TextFileOutputMeta> {

    @Override
    public boolean isDataDriven( TextFileOutputMeta meta ) {
      // We can safely assume that the StepMetaInterface object we get back is a TextFileOutputMeta
      return meta.isFileNameInField();
    }

    @Override
    public Collection<IExternalResourceInfo> getResourcesFromMeta( TextFileOutputMeta meta ) {
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

                    IExternalResourceInfo resource = getFileResource( KettleVFS.getFileObject( path ), false );
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
      TextFileOutputMeta meta, RowMetaInterface rowMeta, Object[] row ) {
      Collection<IExternalResourceInfo> resources = new LinkedList<IExternalResourceInfo>();

      try {
        String filename = rowMeta.getString( row, meta.getFileNameField(), null );
        if ( !Const.isEmpty( filename ) ) {
          FileObject fileObject = KettleVFS.getFileObject( filename );
          resources.add( getFileResource( fileObject, false ) );
        }
      } catch ( KettleException kve ) {
        // TODO throw exception or ignore?
      }

      return resources;
    }

    @Override
    public Class<TextFileOutputMeta> getStepMetaClass() {
      return TextFileOutputMeta.class;
    }
  }

}

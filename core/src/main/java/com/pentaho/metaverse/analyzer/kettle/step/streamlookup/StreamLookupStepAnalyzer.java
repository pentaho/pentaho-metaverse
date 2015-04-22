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

package com.pentaho.metaverse.analyzer.kettle.step.streamlookup;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.streamlookup.StreamLookupMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class StreamLookupStepAnalyzer extends BaseStepAnalyzer<StreamLookupMeta> {

  private static final Logger LOGGER = LoggerFactory.getLogger( BaseStepAnalyzer.class );

  protected String[] keyLookups;
  protected String[] keyStreams;
  protected String[] values;
  protected String[] valueNames;

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> set = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    set.add( StreamLookupMeta.class );
    return set;
  }

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, StreamLookupMeta streamLookupMeta )
    throws MetaverseAnalyzerException {
    IMetaverseNode node = super.analyze( descriptor, streamLookupMeta );

    keyLookups = streamLookupMeta.getKeylookup();
    keyStreams = streamLookupMeta.getKeystream();
    values = streamLookupMeta.getValue();
    valueNames = streamLookupMeta.getValueName();

    for ( int i = 0; i < keyLookups.length; i++ ) {
      IMetaverseNode keyNode = createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, keyStreams[i] ) );
      metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, keyNode );

      IMetaverseNode keyLookupNode =
        createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, keyLookups[i] ) );
      metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, keyLookupNode );

      // Bidirectional Join
      metaverseBuilder.addLink( keyLookupNode, DictionaryConst.LINK_JOINS, keyNode );
      metaverseBuilder.addLink( keyNode, DictionaryConst.LINK_JOINS, keyLookupNode );

    }

    for ( int i = 0; i < valueNames.length; i++ ) {
      String newFieldName = valueNames[ i ];

      if ( newFieldNameExistsInMainInputStream( newFieldName ) ) {
        // the new field name is going to be renamed with the _N naming convention to make it unique
        RowMetaInterface outputFields = getOutputFields( streamLookupMeta );
        for ( int renameIdx = 1; renameIdx <= valueNames.length; renameIdx++ ) {
          ValueMetaInterface outField = outputFields.searchValueMeta( newFieldName + "_" + renameIdx );
          if ( outField != null ) {
            newFieldName = outField.getName();
            break;
          }
        }
      }

      IMetaverseNode valueNode = createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, values[i] ) );
      IMetaverseNode valueName = createNodeFromDescriptor( getStepFieldOriginDescriptor( descriptor, newFieldName ) );


      // Derives
      metaverseBuilder.addLink( valueNode, DictionaryConst.LINK_DERIVES, valueName );

      // Link
      metaverseBuilder.addLink( node, DictionaryConst.LINK_CREATES, valueName );

    }

    return node;
  }

  protected boolean newFieldNameExistsInMainInputStream( String newFieldName ) {
    if ( parentTransMeta != null ) {
      String lookupStepName = baseStepMeta.getStepIOMeta().getInfoStreams().get( 0 ).getStepname();
      String[] prevStepNames = parentTransMeta.getPrevStepNames( parentStepMeta );
      for ( int i = 0; i < prevStepNames.length; i++ ) {
        String prevStepName = prevStepNames[ i ];
        if ( !prevStepName.equals( lookupStepName ) ) {
          try {
            RowMetaInterface stepFields = parentTransMeta.getStepFields( prevStepName );
            if ( stepFields != null ) {
              ValueMetaInterface valueMetaInterface = stepFields.searchValueMeta( newFieldName );
              return valueMetaInterface != null;
            }
          } catch ( KettleStepException e ) {
            LOGGER.warn( Messages.getString( "WARNING.CannotDetermineRowMeta", prevStepName, e.toString() ) );
          }
        }
      }
    }
    return false;
  }

  @Override
  public Map<String, RowMetaInterface> getInputFields( StreamLookupMeta meta ) {
    Map<String, RowMetaInterface> rowMeta = super.getInputFields( meta );

    if ( parentTransMeta != null ) {
      for ( String prevStepName : parentTransMeta.getPrevStepNames( parentStepMeta ) ) {
        if ( !rowMeta.containsKey( prevStepName ) ) {
          try {
            rowMeta.put( prevStepName, parentTransMeta.getStepFields( prevStepName ) );
          } catch ( KettleStepException e ) {
            LOGGER.warn( Messages.getString( "WARNING.CannotDetermineRowMeta", prevStepName, e.toString() ) );
          }
        }
      }
    }
    return rowMeta;
  }

  // ******** Start - Used to aid in unit testing **********
  public void setParentTransMeta( TransMeta parentTransMeta ) {
    this.parentTransMeta = parentTransMeta;
  }

  public void setParentStepMeta( StepMeta parentStepMeta ) {
    this.parentStepMeta = parentStepMeta;
  }
  // ******** End - Used to aid in unit testing **********

}

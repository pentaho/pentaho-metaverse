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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.streamlookup.StreamLookupMeta;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.messages.Messages;

import edu.emory.mathcs.backport.java.util.Arrays;

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

    for ( int i = 0; i < values.length; i++ ) {
      IMetaverseNode keyNode = createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, keyStreams[i] ) );
      metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, keyNode );

      IMetaverseNode keyLookupNode =
          createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, keyLookups[i] ) );
      metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, keyLookupNode );

      IMetaverseNode valueNode = createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, values[i] ) ); // Bidirection
                                                                                                                        // join
      metaverseBuilder.addLink( keyLookupNode, DictionaryConst.LINK_JOINS, valueNode );
      metaverseBuilder.addLink( valueNode, DictionaryConst.LINK_JOINS, keyLookupNode );

      IMetaverseNode valueName = createNodeFromDescriptor( getStepFieldOriginDescriptor( descriptor, valueNames[i] ) );
      metaverseBuilder.addLink( valueNode, DictionaryConst.LINK_DERIVES, valueName );
      metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_CREATES, valueName );
    }

    return node;
  }

  @Override
  protected boolean fieldNameExistsInInput( String fieldName ) {
    boolean result = super.fieldNameExistsInInput( fieldName );
    if (result) {
      return true;
    }
    List<String> inputs = new ArrayList<String>();
    if ( keyLookups != null ) {
      inputs.addAll( Arrays.asList( keyLookups ) );
    }
    if ( keyStreams != null ) {
      inputs.addAll( Arrays.asList( keyStreams ) );
    }
    if ( values != null ) {
      inputs.addAll( Arrays.asList( values ) );
    }
    if ( valueNames != null ) {
      inputs.addAll( Arrays.asList( valueNames ) );
    }
    return inputs.contains( fieldName );
  }

  @Override
  public Map<String, RowMetaInterface> getInputFields( StreamLookupMeta meta ) {
    Map<String, RowMetaInterface> rowMeta = super.getInputFields( meta );
    
    if ( parentTransMeta != null ) {
        for ( String prevStepName : parentTransMeta.getStepNames() ) {
          if ( !rowMeta.containsKey( prevStepName ) ) {
            try {
              rowMeta.put( prevStepName, parentTransMeta.getPrevStepFields( prevStepName ) );
            } catch ( KettleStepException e ) {
              LOGGER.warn( Messages.getString( "WARNING.CannotDetermineRowMeta" ) );
            }
          }
        }
    }
    return rowMeta;
  }

  // ******** Start - Used to aid in unit testing **********
  protected void setKeyLookups( String[] keyLookups ) {
    this.keyLookups = keyLookups;
  }

  protected void setKeyStreams( String[] keyStreams ) {
    this.keyStreams = keyStreams;
  }

  protected void setValues( String[] values ) {
    this.values = values;
  }

  protected void setValueNames( String[] valueNames ) {
    this.valueNames = valueNames;
  }

  public void setParentTransMeta( TransMeta parentTransMeta ) {
    this.parentTransMeta = parentTransMeta;
  }

  public void setParentStepMeta( StepMeta parentStepMeta ) {
    this.parentStepMeta = parentStepMeta;
  }
  // ******** End - Used to aid in unit testing **********

}

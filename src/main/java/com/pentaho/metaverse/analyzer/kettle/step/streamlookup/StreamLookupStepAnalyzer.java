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

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.streamlookup.StreamLookupMeta;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;

public class StreamLookupStepAnalyzer extends BaseStepAnalyzer<StreamLookupMeta> {

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

    String[] keyLookups = streamLookupMeta.getKeylookup();
    String[] keyStreams = streamLookupMeta.getKeystream();
    String[] values = streamLookupMeta.getValue();
    String[] valueNames = streamLookupMeta.getValueName();

    for ( int i = 0; i < keyLookups.length; i++ ) {
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
    }

    return node;
  }

}

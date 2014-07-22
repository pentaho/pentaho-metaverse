/*!
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

package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.DictionaryHelper;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

/**
 * The JobEntryAnalyzer provides JobEntryCopy metadata to the metaverse.
 *
 * Created by gmoran on 7/16/14.
 */
public class JobEntryAnalyzer extends AbstractAnalyzer<JobEntryCopy> {

  @Override
  public IMetaverseNode analyze( JobEntryCopy entry ) throws MetaverseAnalyzerException {

    if ( entry == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.TableOutputMeta.IsNull" ) );
    }

    JobEntryInterface jobEntryInterface = entry.getEntry();

    if ( jobEntryInterface == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.JobEntryInterface.IsNull" ) );
    }

    if ( metaverseBuilder == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseBuilder.IsNull" ) );
    }

    if ( metaverseObjectFactory == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseObjectFactory.IsNull" ) );
    }

    // Add yourself
    IMetaverseNode node = metaverseObjectFactory.createNodeObject(
        DictionaryHelper.getId( entry.getClass(), entry.getName() ) );

    node.setName( entry.getName() );
    node.setType( DictionaryConst.NODE_TYPE_JOB_ENTRY );

    metaverseBuilder.addNode( node );


    return node;

  }

}

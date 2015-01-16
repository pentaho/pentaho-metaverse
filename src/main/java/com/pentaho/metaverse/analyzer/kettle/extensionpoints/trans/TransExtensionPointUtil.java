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
package com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.impl.MetaverseBuilder;
import com.pentaho.metaverse.impl.Namespace;
import com.pentaho.metaverse.messages.Messages;
import com.pentaho.metaverse.util.MetaverseUtil;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.metaverse.IDocument;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseException;

import java.net.URLConnection;

/**
 * This class offers helper methods for Transformation Extension Points used by the lineage capability.
 */
public class TransExtensionPointUtil {

  public static void addLineageGraph( final TransMeta transMeta ) throws MetaverseException {

    if ( transMeta == null ) {
      throw new MetaverseException( Messages.getString( "ERROR.Document.IsNull" ) );
    }

    final Graph graph = new TinkerGraph();
    final IMetaverseBuilder metaverseBuilder = new MetaverseBuilder( graph );
    final IMetaverseObjectFactory objFactory = metaverseBuilder.getMetaverseObjectFactory();

    // Add the client design node
    final String clientName = KettleClientEnvironment.getInstance().getClient().toString();
    final INamespace namespace = new Namespace( clientName );

    final IMetaverseNode designNode =
      objFactory.createNodeObject( clientName, clientName, DictionaryConst.NODE_TYPE_LOCATOR );
    metaverseBuilder.addNode( designNode );

    // Create a document object containing the transMeta
    final IDocument document = MetaverseUtil.createDocument(
      objFactory,
      namespace,
      transMeta,
      transMeta.getFilename(),
      transMeta.getName(),
      "ktr",
      URLConnection.getFileNameMap().getContentTypeFor( transMeta.getFilename() )
    );

    MetaverseUtil.addLineageGraph( document, graph );
  }
}

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


package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.impl.MetaverseBuilder;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.metaverse.util.MetaverseUtil;

import java.net.URLConnection;

/**
 * This class offers helper methods for Transformation Extension Points used by the lineage capability.
 */
public class TransExtensionPointUtil {

  public static void addLineageGraph( final TransMeta transMeta ) throws MetaverseException {

    if ( transMeta == null ) {
      throw new MetaverseException( Messages.getString( "ERROR.Document.IsNull" ) );
    }

    // Get the "natural" filename (repo-based if in repository, filesystem-based otherwise)
    String filename = getFilename( transMeta );

    final Graph graph = new TinkerGraph();
    final IMetaverseBuilder metaverseBuilder = new MetaverseBuilder( graph );
    final IMetaverseObjectFactory objFactory = MetaverseUtil.getDocumentController().getMetaverseObjectFactory();

    // Add the client design node
    final String clientName = KettleClientEnvironment.getInstance().getClient().toString();
    final INamespace namespace = new Namespace( clientName );

    final IMetaverseNode designNode =
      objFactory.createNodeObject( clientName, clientName, DictionaryConst.NODE_TYPE_LOCATOR );
    metaverseBuilder.addNode( designNode );

    // Create a document object containing the transMeta
    final IDocument document = MetaverseUtil.createDocument(
      namespace,
      transMeta,
      filename,
      transMeta.getName(),
      transMeta.getDefaultExtension(),
      URLConnection.getFileNameMap().getContentTypeFor( "trans.ktr" )
    );

    MetaverseUtil.addLineageGraph( document, graph );
  }

  public static String getFilename( TransMeta transMeta ) {
    return KettleAnalyzerUtil.getFilename( transMeta );
  }
}

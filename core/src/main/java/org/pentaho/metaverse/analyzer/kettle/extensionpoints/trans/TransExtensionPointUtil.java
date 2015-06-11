/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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

    // Don't analyze the transformation until it has been saved (i.e. has a filename)
    if ( transMeta.getFilename() == null ) {
      throw new MetaverseException( Messages.getString( "ERROR.Document.NotSaved" ) );
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
      "ktr",
      URLConnection.getFileNameMap().getContentTypeFor( "trans.ktr" )
    );

    MetaverseUtil.addLineageGraph( document, graph );
  }

  public static String getFilename( TransMeta transMeta ) {
    String filename = transMeta.getFilename();
    if ( filename == null ) {
      filename = transMeta.getPathAndName();
      if ( transMeta.getDefaultExtension() != null ) {
        filename = filename + "." + transMeta.getDefaultExtension();
      }
    }
    return filename;
  }
}

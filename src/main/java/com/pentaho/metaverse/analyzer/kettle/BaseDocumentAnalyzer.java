package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IDocument;
import org.pentaho.platform.api.metaverse.IDocumentAnalyzer;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

/**
 * Created by gmoran on 8/11/14.
 */
public abstract class BaseDocumentAnalyzer extends BaseKettleMetaverseComponent
  implements IDocumentAnalyzer<IMetaverseNode> {

  /**
   * This method handles null checks for state validation
   *
   * @param document the document that will be processed in non-abstract subclasses
   * @throws MetaverseAnalyzerException
   */
  protected void validateState( IDocument document ) throws MetaverseAnalyzerException {

    if ( document == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.Document.IsNull" ) );
    }

    Object repoObject = document.getContent();

    if ( repoObject == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.Document.HasNoContent" ) );
    }

    if ( metaverseObjectFactory == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseObjectFactory.IsNull" ) );
    }

    if ( metaverseBuilder == null ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MetaverseBuilder.IsNull" ) );
    }

  }

  /**
   * This method creates the document
   * node relationships with the system level structural and data nodes in the graph.
   *
   * @param child the document node
   * @throws MetaverseAnalyzerException
   */
  public void addParentLink( IComponentDescriptor descriptor, IMetaverseNode child )
    throws MetaverseAnalyzerException {

    // The document is always a child of the locator. If this nis not the case, then do not
    // subclass this document analyzer.
    //
    // This will create a virtual node that will line up with the correct
    // locator node for this document in the graph.
    IMetaverseNode locatorNode =
        metaverseObjectFactory.createNodeObject( descriptor.getStringID() );

    metaverseBuilder.addLink( locatorNode, DictionaryConst.LINK_CONTAINS, child );

  }

}

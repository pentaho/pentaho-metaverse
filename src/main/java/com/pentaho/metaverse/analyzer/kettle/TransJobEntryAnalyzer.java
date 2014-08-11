package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.DictionaryHelper;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;


/**
 * Created by gmoran on 7/30/14.
 */
public class TransJobEntryAnalyzer extends JobEntryAnalyzer<JobEntryTrans> {

  @Override
  public IMetaverseNode analyze( JobEntryTrans entry ) throws MetaverseAnalyzerException {

    IMetaverseNode node =  super.analyze( entry );

    String filename =  entry.getParentJob().getJobMeta().environmentSubstitute( entry.getFilename() );

    IMetaverseNode transformationNode = metaverseObjectFactory.createNodeObject(
        DictionaryHelper.getId(IMetaverseDocument.class,
            getNamespace().getParentNamespace().getNamespaceId(), filename )
    );

    transformationNode.setName( entry.getName() );
    transformationNode.setType( DictionaryConst.NODE_TYPE_TRANS );

    metaverseBuilder.addLink( node, DictionaryConst.LINK_CONTAINS, transformationNode );

    return node;
  }


}

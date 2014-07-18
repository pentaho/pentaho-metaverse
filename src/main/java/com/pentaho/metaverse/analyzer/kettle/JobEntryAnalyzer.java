package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.DictionaryHelper;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

/**
 * Created by gmoran on 7/16/14.
 */
public class JobEntryAnalyzer extends AbstractAnalyzer<JobEntryCopy> {

  @Override
  public IMetaverseNode analyze( JobEntryCopy entry ) throws MetaverseAnalyzerException {

    if ( entry == null ) {
      throw new MetaverseAnalyzerException( "TableOutputMeta is null!" );
    }

    JobEntryInterface jobEntryInterface = entry.getEntry();

    if ( jobEntryInterface == null ) {
      throw new MetaverseAnalyzerException( "JobEntryInterface is null!" );
    }

    if ( metaverseBuilder == null ) {
      throw new MetaverseAnalyzerException( "MetaverseBuilder is null!" );
    }

    if ( metaverseObjectFactory == null ) {
      throw new MetaverseAnalyzerException( "MetaverseObjectFactory is null!" );
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

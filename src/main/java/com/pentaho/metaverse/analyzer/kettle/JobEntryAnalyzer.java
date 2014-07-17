package com.pentaho.metaverse.analyzer.kettle;

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
    IMetaverseNode node = metaverseObjectFactory.createNodeObject( "TODO" );

    node.setName( entry.getName() );

    // TODO What are these types? They belong in the dictionary, yes?
    node.setType( "jobEntry" );

    metaverseBuilder.addNode( node );


    return node;

  }

}

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

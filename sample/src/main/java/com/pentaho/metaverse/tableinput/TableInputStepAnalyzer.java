package com.pentaho.metaverse.tableinput;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.Namespace;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The TableInputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and
 * other metaverse entities
 */
public class TableInputStepAnalyzer extends BaseStepAnalyzer<TableInputMeta> {
  private Logger log = LoggerFactory.getLogger( TableInputStepAnalyzer.class );

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, TableInputMeta tableFileInputMeta )
      throws MetaverseAnalyzerException {

    // do the common analysis for all step
    super.analyze( descriptor, tableFileInputMeta );

    // NOTE: We are assuming for this POC that the column name matches the stream field name, meaning
    // the Table Input SQL script doesn't use aliases, aggregates, etc.
    if ( stepFields != null ) {
      List<ValueMetaInterface> stepFieldValueMetas = stepFields.getValueMetaList();
      for ( ValueMetaInterface fieldMeta : stepFieldValueMetas ) {
        String fieldName = fieldMeta.getName();
        IComponentDescriptor dbColumnDescriptor = new MetaverseComponentDescriptor(
            fieldName,
            DictionaryConst.NODE_TYPE_DATA_COLUMN,
            new Namespace( fieldName ),
            descriptor.getContext() );
        IMetaverseNode dbColumnNode = createNodeFromDescriptor( dbColumnDescriptor );
        metaverseBuilder.addNode( dbColumnNode );

        // Get the stream field output from this step. It should've already been created when we called super.analyze()
        IComponentDescriptor transFieldDescriptor = getStepFieldOriginDescriptor( descriptor, fieldName );
        IMetaverseNode outNode = createNodeFromDescriptor( transFieldDescriptor );

        metaverseBuilder.addLink( dbColumnNode, DictionaryConst.LINK_POPULATES, outNode );

        // add a link from the fileField to the text file input step node
        metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, dbColumnNode );
      }
    }

    return rootNode;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( TableInputMeta.class );
      }
    };
  }
}

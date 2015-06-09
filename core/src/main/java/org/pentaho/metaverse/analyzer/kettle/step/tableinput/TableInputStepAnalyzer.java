package org.pentaho.metaverse.analyzer.kettle.step.tableinput;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.ConnectionExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.model.BaseDatabaseResourceInfo;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * The TableInputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and other
 * metaverse entities
 */
public class TableInputStepAnalyzer extends ConnectionExternalResourceStepAnalyzer<TableInputMeta> {
  private Logger log = LoggerFactory.getLogger( TableInputStepAnalyzer.class );

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( TableInputMeta.class );
      }
    };
  }

  @Override
  protected IMetaverseNode createTableNode( IExternalResourceInfo resource ) throws MetaverseAnalyzerException {
    BaseDatabaseResourceInfo resourceInfo = (BaseDatabaseResourceInfo) resource;

    Object obj = resourceInfo.getAttributes().get( DictionaryConst.PROPERTY_QUERY );
    String query = obj == null ? null : obj.toString();

    // create a node for the table
    MetaverseComponentDescriptor componentDescriptor = new MetaverseComponentDescriptor(
      "SQL",
      DictionaryConst.NODE_TYPE_SQL_QUERY,
      getConnectionNode(),
      getDescriptor().getContext() );

    // set the namespace to be the id of the connection node.
    IMetaverseNode tableNode = createNodeFromDescriptor( componentDescriptor );
    tableNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, componentDescriptor.getNamespace().getNamespaceId() );
    tableNode.setProperty( DictionaryConst.PROPERTY_QUERY, query );
    tableNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DB_QUERY );
    return tableNode;
  }

  @Override
  public String getResourceInputNodeType() {
    return DictionaryConst.NODE_TYPE_DATA_COLUMN;
  }

  @Override
  public String getResourceOutputNodeType() {
    return null;
  }

  @Override
  public boolean isOutput() {
    return false;
  }

  @Override
  public boolean isInput() {
    return true;
  }

  @Override
  protected Set<StepField> getUsedFields( TableInputMeta meta ) {
    return null;
  }

  @Override
  public IMetaverseNode getConnectionNode() throws MetaverseAnalyzerException {
    if ( connectionNode == null ) {
      connectionNode = (IMetaverseNode) getConnectionAnalyzer().analyze(
        getDescriptor(), baseStepMeta.getDatabaseMeta() );
    }
    return connectionNode;
  }

  //////////////
  public void setBaseStepMeta( TableInputMeta meta ) {
    baseStepMeta = meta;
  }
}

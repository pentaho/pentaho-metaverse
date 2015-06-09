/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.analyzer.kettle.step.getxmldata;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataField;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The GetXMLDataStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and
 * other metaverse entities
 */
public class GetXMLDataStepAnalyzer extends ExternalResourceStepAnalyzer<GetXMLDataMeta> {
  private Logger log = LoggerFactory.getLogger( GetXMLDataStepAnalyzer.class );

  @Override
  protected Set<StepField> getUsedFields( GetXMLDataMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();
    if ( meta.isInFields() ) {
      Set<StepField> stepFields = createStepFields( meta.getXMLField(), getInputs() );
      usedFields.addAll( stepFields );
    }
    return usedFields;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( GetXMLDataMeta.class );
      }
    };
  }

  @Override
  protected IMetaverseNode createOutputFieldNode( IAnalysisContext context, ValueMetaInterface fieldMeta,
                                                  String targetStepName, String nodeType ) {
    IMetaverseNode fieldNode = super.createOutputFieldNode( context, fieldMeta, targetStepName, nodeType );
    GetXMLDataField[] fields = baseStepMeta.getInputFields();
    for ( GetXMLDataField field : fields ) {
      if ( fieldMeta.getName().equals( field.getName() ) ) {
        fieldNode.setProperty( "xpath", Const.NVL( field.getXPath(), "" ) );
        fieldNode.setProperty( "element", Const.NVL( field.getElementTypeCode(), "" ) );
        fieldNode.setProperty( "resultType", Const.NVL( field.getResultTypeCode(), "" ) );
        fieldNode.setProperty( "repeat", field.isRepeated() );
        break;
      }
    }
    return fieldNode;
  }

  @Override
  protected Map<String, RowMetaInterface> getInputRowMetaInterfaces( GetXMLDataMeta meta ) {
    Map<String, RowMetaInterface> inputRows = getInputFields( meta );
    if ( inputRows == null ) {
      inputRows = new HashMap<>();
    }
    // Get some boolean flags from the meta for easier access
    boolean isInFields = meta.isInFields();
    boolean isAFile = meta.getIsAFile();
    boolean isAUrl = meta.isReadUrl();

    // only add resource fields if we are NOT getting the xml or file from a field
    if ( !isInFields || isAFile || isAUrl ) {
      RowMetaInterface stepFields = getOutputFields( meta );
      RowMetaInterface clone = stepFields.clone();
      // if there are previous steps providing data, we should remove them from the set of "resource" fields
      for ( RowMetaInterface rowMetaInterface : inputRows.values() ) {
        for ( ValueMetaInterface valueMetaInterface : rowMetaInterface.getValueMetaList() ) {
          try {
            clone.removeValueMeta( valueMetaInterface.getName() );
          } catch ( KettleValueException e ) {
            // could not find it in the output, skip it
          }
        }
      }
      inputRows.put( RESOURCE, clone );
    }
    return inputRows;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( GetXMLDataMeta meta )
    throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changes = new HashSet<>();

    boolean isInFields = meta.isInFields();
    boolean isAFile = meta.getIsAFile();
    boolean isAUrl = meta.isReadUrl();

    // if we are getting xml from a field, we need to add the "derives" links from the xml to the output fields
    if ( isInFields && !isAFile && !isAUrl ) {
      GetXMLDataField[] fields = baseStepMeta.getInputFields();
      if ( getInputs() != null ) {
        Set<StepField> inputFields = getInputs().getFieldNames();

        for ( StepField inputField : inputFields ) {
          if ( inputField.getFieldName().equals( meta.getXMLField() ) ) {
            // link this to all of the outputs that come from the xml
            for ( GetXMLDataField field : fields ) {
              ComponentDerivationRecord change = new ComponentDerivationRecord( meta.getXMLField(), field.getName() );
              changes.add( change );
            }
            break;
          }
        }
      }
    }
    return changes;
  }

  @Override
  protected void customAnalyze( GetXMLDataMeta meta, IMetaverseNode node ) throws MetaverseAnalyzerException {
    super.customAnalyze( meta, node );
    // Add the XPath Loop to the step node
    node.setProperty( "loopXPath", meta.getLoopXPath() );
  }

  @Override
  public IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException {
    return createFileNode( resource.getName(), descriptor );
  }

  @Override
  public String getResourceInputNodeType() {
    return DictionaryConst.NODE_TYPE_FILE_FIELD;
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

  ///// used for unit testing
  protected void setObjectFactory( IMetaverseObjectFactory factory ) {
    this.metaverseObjectFactory = factory;
  }
  protected void setRootNode( IMetaverseNode node ) {
    rootNode = node;
  }
  protected void setBaseStepMeta( GetXMLDataMeta meta ) {
    baseStepMeta = meta;
  }
  protected void setParentTransMeta( TransMeta tm ) {
    parentTransMeta = tm;
  }
  protected void setParentStepMeta( StepMeta sm ) {
    parentStepMeta = sm;
  }
  ///// used for unit testing

}

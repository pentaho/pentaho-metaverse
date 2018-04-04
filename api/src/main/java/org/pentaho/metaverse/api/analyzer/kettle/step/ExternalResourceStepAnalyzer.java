/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.apache.commons.collections.MapUtils;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.file.BaseFileInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.messages.Messages;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ExternalResourceStepAnalyzer<T extends BaseStepMeta> extends StepAnalyzer<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger( ExternalResourceStepAnalyzer.class );
  public static final String RESOURCE = "_resource_";

  private IStepExternalResourceConsumer externalResourceConsumer;

  @Override
  protected void customAnalyze( T meta, IMetaverseNode node ) throws MetaverseAnalyzerException {
    // handle all of the external resources
    if ( getExternalResourceConsumer() != null ) {
      IAnalysisContext context = getDescriptor().getContext();
      Collection<IExternalResourceInfo> resources = getExternalResourceConsumer().getResourcesFromMeta( meta, context );
      for ( IExternalResourceInfo resource : resources ) {
        try {
          if ( resource.isInput() ) {
            String label = DictionaryConst.LINK_READBY;
            IMetaverseNode resourceNode = createResourceNode( resource );
            getMetaverseBuilder().addNode( resourceNode );
            getMetaverseBuilder().addLink( resourceNode, label, node );
          }
          if ( resource.isOutput() ) {
            String label = DictionaryConst.LINK_WRITESTO;
            IMetaverseNode resourceNode = createResourceNode( resource );
            getMetaverseBuilder().addNode( resourceNode );
            getMetaverseBuilder().addLink( node, label, resourceNode );
          }
        } catch ( MetaverseException e ) {
          LOGGER.warn( e.getLocalizedMessage() );
          LOGGER.debug( Messages.getString( "ERROR.ErrorDuringAnalysisStackTrace" ), e );
        }
      }
    }
  }

  /**
   * Returns true if the step has an output resource. This will be true in most cases, except for steps that may
   * choose to pass the data as a value or to some target other than an external resource (such as a servlet).
   *
   * @return rue if the step has an output resource
   */
  public boolean hasOutputResource( T meta ) {
    return true;
  }

  @Override
  protected Map<String, RowMetaInterface> getOutputRowMetaInterfaces( T meta ) {
    Map<String, RowMetaInterface> outputRows = super.getOutputRowMetaInterfaces( meta );
    if ( hasOutputResource( meta ) && MapUtils.isNotEmpty( outputRows ) ) {
      // if this is an output step analyzer, we always need to write the resource fields out
      if ( isOutput() ) {
        RowMetaInterface out = null;
        Set<String> outputResourceFields = getOutputResourceFields( meta );
        for ( RowMetaInterface rowMetaInterface : outputRows.values() ) {
          if ( outputResourceFields != null ) {
            out = rowMetaInterface.clone();

            // only add the fields that appear in the output of the step, not all fields that pass through
            for ( ValueMetaInterface field : rowMetaInterface.getValueMetaList() ) {
              if ( !outputResourceFields.contains( field.getName() ) ) {
                try {
                  out.removeValueMeta( field.getName() );
                } catch ( KettleValueException e ) {
                  // could not find it in the output, skip it
                }
              }
            }
          } else {
            // assume all fields are written
            out = rowMetaInterface;
          }
          break;
        }

        outputRows.put( RESOURCE, out );
      }
    }

    return outputRows;
  }

  @Override
  protected Map<String, RowMetaInterface> getInputRowMetaInterfaces( T meta ) {
    Map<String, RowMetaInterface> inputRows = super.getInputRowMetaInterfaces( meta );
    if ( inputRows == null ) {
      inputRows = new HashMap<>();
    }
    // assume that the output fields are defined in the step and are based on the resource inputs
    if ( isInput() ) {
      RowMetaInterface stepFields = getOutputFields( meta );
      if ( stepFields != null ) {
        RowMetaInterface clone = stepFields.clone();
        // ignore any input fields that are not specifically defined as meta inputs, for example, all the
        // "additional" fields, or fields coming from previous steps
        for ( final String inputFieldToIgnore : getInputFieldsToIgnore( meta, inputRows, stepFields ) ) {
          try {
            clone.removeValueMeta( inputFieldToIgnore );
          } catch ( KettleValueException e ) {
            // could not find it in the output, skip it
          }
        }
        inputRows.put( RESOURCE, clone );
      }
    }
    return inputRows;
  }

  /**
   * Returns a {@lnk Set} of field names that are to be ignored when fetching "resource" fields. This set will
   * typically include any field that is either provided by a previous step, or is some "additional" field.
   */
  protected Set<String> getInputFieldsToIgnore( final T meta, final Map<String, RowMetaInterface> inputRows,
                                                 final RowMetaInterface rowMeta ) {
    Set<String> inputFieldsToIgnore = new HashSet<>();
    for ( RowMetaInterface rowMetaInterface : inputRows.values() ) {
      for ( ValueMetaInterface inputField : rowMetaInterface.getValueMetaList() ) {
        inputFieldsToIgnore.add( inputField.getName() );
      }
    }
    if ( meta instanceof BaseFileInputMeta ) {
      final BaseFileInputMeta fileMeta = (BaseFileInputMeta) meta;
      final BaseFileField[] inputFields = fileMeta.getInputFields();
      final List<String> inputFieldNames = new ArrayList<>();
      for ( final BaseFileField inputField : inputFields ) {
        inputFieldNames.add( inputField.getName() );
      }
      // get the fields within rowMeta - any field tha ISN'T a meta input field, should be ignored
      final List<ValueMetaInterface> rowMetaValueMetaList = rowMeta.getValueMetaList();
      for ( final ValueMetaInterface rowMetaValueMeta : rowMetaValueMetaList ) {
        if ( !inputFieldNames.contains( rowMetaValueMeta.getName() ) ) {
          inputFieldsToIgnore.add( rowMetaValueMeta.getName() );
        }
      }
    }
    return inputFieldsToIgnore;
  }

  @Override
  protected IMetaverseNode createOutputFieldNode( IAnalysisContext context, ValueMetaInterface fieldMeta,
                                                  String targetStepName, String nodeType ) {

    // if the targetStepName is 'resource' then this is HAS to be a resource field
    nodeType = RESOURCE.equals( targetStepName ) ? getResourceOutputNodeType() : nodeType;
    return super.createOutputFieldNode( context, fieldMeta, targetStepName, nodeType );
  }

  @Override
  protected IMetaverseNode createInputFieldNode( IAnalysisContext context, ValueMetaInterface fieldMeta,
                                                 String previousStepName, String nodeType ) {

    // if the previousStepName is 'resource' then this is HAS to be a resource field
    boolean isResource = RESOURCE.equals( previousStepName );
    nodeType = isResource ? getResourceInputNodeType() : nodeType;
    IMetaverseNode inputFieldNode = super.createInputFieldNode( context, fieldMeta, previousStepName, nodeType );
    inputFieldNode.setType( nodeType );
    if ( isResource ) {
      // add the node so it's not virtual
      getMetaverseBuilder().addNode( inputFieldNode );
    }
    return inputFieldNode;
  }

  public IStepExternalResourceConsumer getExternalResourceConsumer() {
    return externalResourceConsumer;
  }

  public void setExternalResourceConsumer( IStepExternalResourceConsumer externalResourceConsumer ) {
    this.externalResourceConsumer = externalResourceConsumer;
  }

  /**
   * Get the resource fields actually written to the external resource (file, table, ...). Return of null assumes
   * that all fields that are reported when calling getStepFields are written. If that is NOT the case, override this
   * method and return the field names that are actually written.
   *
   * @return
   */
  public Set<String> getOutputResourceFields( T meta ) {
    return null;
  }

  @Override
  protected void linkChangeNodes( IMetaverseNode inputNode, IMetaverseNode outputNode ) {
    // figure out the correct label to add based on the type of nodes we are dealing with
    boolean nodeTypesMatch = inputNode.getType().equals( outputNode.getType() );
    if ( !nodeTypesMatch && ( isInput() || isOutput() ) ) {
      getMetaverseBuilder().addLink( inputNode, DictionaryConst.LINK_POPULATES, outputNode );
    } else {
      getMetaverseBuilder().addLink( inputNode, getInputToOutputLinkLabel(), outputNode );
    }
  }

  public abstract IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException;

  public abstract String getResourceInputNodeType();

  public abstract String getResourceOutputNodeType();

  public abstract boolean isOutput();

  public abstract boolean isInput();

}

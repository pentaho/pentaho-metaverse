/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.mapping;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.trans.ISubTransAwareMeta;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.mapping.MappingIODefinition;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseMappingAnalyzer<T extends StepWithMappingMeta> extends StepAnalyzer<StepWithMappingMeta> {

  @Override
  protected Set<StepField> getUsedFields( final StepWithMappingMeta meta ) {
    final Set<StepField> usedFields = new HashSet();
    return usedFields;
  }

  @Override
  protected void customAnalyze( final StepWithMappingMeta meta, final IMetaverseNode rootNode )
    throws MetaverseAnalyzerException {

    final String transformationPath = parentTransMeta.environmentSubstitute( meta.getTransName() );
    rootNode.setProperty( "subTransformation", transformationPath );

    final TransMeta subTransMeta = KettleAnalyzerUtil.getSubTransMeta( (ISubTransAwareMeta) meta );

    final IMetaverseNode subTransNode = getNode( subTransMeta.getName(), DictionaryConst.NODE_TYPE_TRANS,
      descriptor.getNamespace(), null, null );
    subTransNode.setProperty( DictionaryConst.PROPERTY_PATH,
      KettleAnalyzerUtil.getSubTransMetaPath( (ISubTransAwareMeta) meta, subTransMeta ) );
    subTransNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_EXECUTES, subTransNode );

    // ----------- analyze inputs, for each input:
    // - create a virtual step node for the input source step, the step name is either defined "input source step
    //   name", or if "main data path" is selected, it is the first encountered input step into the mapping step
    // - create a virtual step node for the mapping target step, the step name is defined in "mapping target step name",
    //   or if "main data path" is selected we need fake it since we will not have access to it, name it something
    //   like: [<sub-transformation>]: mapping input specification step name
    // - create virtual field nodes for all fields defined in the "fieldname to mapping input step" column and
    //   "derives" links from the source step fields defined in the "fieldname from source step" column that belong
    //   to the input source steps to these new virtual fields
    final List<String> verboseProps = new ArrayList();
    processInputMappings( subTransMeta, verboseProps, meta.getInputMappings() );

    // ----------- analyze outputs, for each output:
    // - create a virtual step node for the mapping source step, the step name is either defined "mapping source
    //   step name, or if "main data path" is selected we need fake it since we will not have access to it, name it
    //   something like: [<sub-transformation>]: mapping output specification step name
    // - create a virtual step node for the output target step, the step name is defined in "output target step name",
    //   or if "main data path" is is the first output step encountered that the mapping step outputs into
    // - create virtual field nodes for all fields defined in the "fieldname from mapping step" column and
    //   "derives" links from these new virtual fields to the fields defined in the "fieldsname to target step" that
    //   belong to the output target step
    processOutputMappings( subTransMeta, verboseProps, meta.getOutputMappings() );

    // When "Update mapped field names" is selected, the name of the parent field is used, rather than the sub-trans
    // field and vice versa
    createOutputFields( subTransMeta, subTransNode, meta );
    rootNode.setProperty( DictionaryConst.PROPERTY_VERBOSE_DETAILS, StringUtils.join( verboseProps, "," ) );
  }

  private boolean shouldRenameFields( final StepWithMappingMeta meta ) {
    for ( final MappingIODefinition inputMapping : meta.getInputMappings() ) {
      // if ANY one of the mappings has the rename flag set, we are renaming fields
      if ( inputMapping.isRenamingOnOutput() ) {
        return true;
      }
    }
    return false;
  }

  private void createOutputFields( final TransMeta subTransMeta, final IMetaverseNode subTransNode,
                                   final StepWithMappingMeta meta ) {
    final Map<String, IMetaverseNode> nodes = new HashMap();
    boolean renameFields = shouldRenameFields( meta );

    for ( final MappingIODefinition inputMapping : meta.getInputMappings() ) {

      // create a virtual node to represent the sub transformation's mapping input target step
      final String subTransInputTargetStepName = getTargetStepName( subTransMeta, inputMapping, false, true );
      IMetaverseNode subTransInputTargetNode = getVirtualNode( subTransInputTargetStepName,
        DictionaryConst.NODE_TYPE_TRANS_STEP, descriptor.getNamespace(), subTransInputTargetStepName, nodes );
      metaverseBuilder.addLink( subTransNode, DictionaryConst.LINK_CONTAINS, subTransInputTargetNode );

      // for every field defined in the input tab, check first which column to get based on the value of
      // 'renameFields', then create a field with that name, with the target field corresponding to the target in the
      // output mapping, if one exists
      for ( final MappingValueRename inputFieldRename : inputMapping.getValueRenames() ) {
        final String inputSubTransFieldName = renameFields ? inputFieldRename.getSourceValueName()
          : inputFieldRename.getTargetValueName();
        final IMetaverseNode outputFieldNode = getNode( inputSubTransFieldName,
          DictionaryConst.NODE_TYPE_TRANS_FIELD, (String) rootNode.getProperty(
            DictionaryConst.PROPERTY_LOGICAL_ID ), rootNode.getName() + ":" + inputSubTransFieldName, nodes );
        metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_OUTPUTS, outputFieldNode );

        // find an input field whose name matches that defined in the input source of the input mapping, if it exists
        final IMetaverseNode matchingInputFieldNode = getInputs().findNode(
          getSourceStepName( subTransMeta, inputMapping, false, true ), inputFieldRename.getSourceValueName() );
        // create a virtual output field representing the rename target - this field is contained by
        // 'subTransInputTargetNode'
        final IMetaverseNode subTransOutputFieldNode = getVirtualNode( inputFieldRename.getTargetValueName(),
          DictionaryConst.NODE_TYPE_TRANS_FIELD, (String) subTransInputTargetNode.getProperty(
            DictionaryConst.PROPERTY_LOGICAL_ID ), subTransInputTargetNode.getName() + ":"
            + inputFieldRename.getTargetValueName(), nodes );
        metaverseBuilder.addLink( subTransInputTargetNode, DictionaryConst.LINK_OUTPUTS, subTransOutputFieldNode );
        // create an "input" link from the matching field to thesubTransOutputFieldNode and a "derives" link from the
        // input node to subTransOutputFieldNode
        if ( matchingInputFieldNode != null ) {
          metaverseBuilder.addLink( matchingInputFieldNode, DictionaryConst.LINK_INPUTS, subTransInputTargetNode );
          metaverseBuilder.addLink( matchingInputFieldNode, DictionaryConst.LINK_DERIVES, subTransOutputFieldNode );
        }
        // and a link from subTransOutputFieldNode to the outputNode
        metaverseBuilder.addLink( subTransOutputFieldNode, DictionaryConst.LINK_DERIVES, outputFieldNode );
      }
    }

    for ( final MappingIODefinition outputMapping : meta.getOutputMappings() ) {
      final String subTransOutputSourceStepName = getSourceStepName( subTransMeta, outputMapping, false, false );

      // ... and if a corresponding outputMapping exists, create a virtual node corresponding to the output source step
      IMetaverseNode subTransOutputSourceNode = getVirtualNode( subTransOutputSourceStepName,
        DictionaryConst.NODE_TYPE_TRANS_STEP, descriptor.getNamespace(), subTransOutputSourceStepName, nodes );
      metaverseBuilder.addLink( subTransNode, DictionaryConst.LINK_CONTAINS, subTransOutputSourceNode );

      // if any fields are defined in for the output mapping and the subTransOutputStepField exists, for each field
      // defined in the output tab, create a virtual field with the name from the source column that the
      // subTransOutputStepField "outputs", a field node with the name from the target column that the root node
      // outputs and and a "derives" link between the fields
      for ( final MappingValueRename outputFieldRename : outputMapping.getValueRenames() ) {
        final String subTransInputFieldName = outputFieldRename.getSourceValueName();
        final IMetaverseNode subTransInputFieldNode = getVirtualNode( subTransInputFieldName,
          DictionaryConst.NODE_TYPE_TRANS_FIELD, (String) subTransOutputSourceNode.getProperty(
            DictionaryConst.PROPERTY_LOGICAL_ID ), subTransOutputSourceNode.getName()
            + ":" + subTransInputFieldName, nodes );
        subTransInputFieldNode.setProperty( DictionaryConst.PROPERTY_TARGET_STEP, rootNode.getName() );
        metaverseBuilder.addLink( subTransOutputSourceNode, DictionaryConst.LINK_OUTPUTS, subTransInputFieldNode );

        final String outputFieldName = outputFieldRename.getTargetValueName();
        final IMetaverseNode outputFieldNode = getNode( outputFieldName,
          DictionaryConst.NODE_TYPE_TRANS_FIELD, (String) rootNode.getProperty(
            DictionaryConst.PROPERTY_LOGICAL_ID ), subTransOutputSourceNode.getName() + ":" + outputFieldName, nodes );
        // set the target step for this field node as the target from the corresponding output mapping
        outputFieldNode.setProperty( DictionaryConst.PROPERTY_TARGET_STEP,
          getTargetStepName( subTransMeta, outputMapping, false, false ) );
        metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_OUTPUTS, outputFieldNode );
        metaverseBuilder.addLink( subTransInputFieldNode, DictionaryConst.LINK_DERIVES, outputFieldNode );
      }
    }
  }

  private void processInputMappings( final TransMeta subTransMeta, final List<String> verboseProps,
                                     final List<MappingIODefinition> inputMappings ) {
    int mappingIdx = 1;
    for ( final MappingIODefinition mapping : inputMappings ) {
      final String mappingKey = "input [" + mappingIdx + "]";
      verboseProps.add( mappingKey );
      String sourceStep = getSourceStepName( subTransMeta, mapping, true, true );
      String targetStep = getTargetStepName( subTransMeta, mapping, true, true );
      final StringBuilder mappingStr = new StringBuilder();
      if ( sourceStep != null && targetStep != null ) {
        mappingStr.append( sourceStep ).append( " > " ).append( targetStep );
      }
      // main path?
      if ( !mapping.isMainDataPath() ) {
        final String descriptionKey = mappingKey + " description";
        verboseProps.add( descriptionKey );
        rootNode.setProperty( descriptionKey, mapping.getDescription() );
      }
      setCommonProps( mappingKey, mapping, mappingStr, verboseProps );
      mappingIdx++;
    }
  }

  private void processOutputMappings( final TransMeta subTransMeta, final List<String> verboseProps,
                                      final List<MappingIODefinition> mappings ) {
    int mappingIdx = 1;
    for ( final MappingIODefinition mapping : mappings ) {
      final String mappingKey = "output [" + mappingIdx + "]";
      verboseProps.add( mappingKey );
      String sourceStep = getSourceStepName( subTransMeta, mapping, true, false );
      String targetStep = getTargetStepName( subTransMeta, mapping, true, false );
      final StringBuilder mappingStr = new StringBuilder();
      if ( sourceStep != null && targetStep != null ) {
        mappingStr.append( sourceStep ).append( " > " ).append( targetStep );
      }
      // main path?
      if ( !mapping.isMainDataPath() ) {
        final String descriptionKey = mappingKey + " description";
        verboseProps.add( descriptionKey );
        rootNode.setProperty( descriptionKey, mapping.getDescription() );
      }
      setCommonProps( mappingKey, mapping, mappingStr, verboseProps );
      mappingIdx++;
    }
  }

  private String getSourceStepName( final TransMeta subTransMeta, final MappingIODefinition mapping,
                                    final boolean includeSubTransPrefix, final boolean input ) {
    String sourceStep = null;
    if ( mapping != null ) {
      if ( input ) {
        sourceStep = mapping.isMainDataPath() ? ( prevStepNames.length > 0 ? prevStepNames[ 0 ] : null )
          : mapping.getInputStepname();
      } else {
        if ( mapping.isMainDataPath() ) {
          final List<TransHopMeta> hops = parentTransMeta.getTransHops();
          for ( final TransHopMeta hop : hops ) {
            if ( hop.getFromStep().equals( parentStepMeta ) ) {
              sourceStep = ( includeSubTransPrefix ? "[" + subTransMeta.getName() + "] " : "" )
                + "<mapping_output_specification>";
              break;
            }
          }
        } else {
          sourceStep = ( includeSubTransPrefix ? "[" + subTransMeta.getName() + "] " : "" )
            + mapping.getInputStepname();
        }
      }
    }
    return sourceStep;
  }

  private String getTargetStepName( final TransMeta subTransMeta, final MappingIODefinition mapping,
                                    final boolean includeSubTransPrefix, final boolean input ) {
    String targetStep = null;
    if ( mapping != null ) {
      if ( input ) {
        targetStep = ( includeSubTransPrefix ? "[" + subTransMeta.getName() + "] " : "" )
          + ( mapping.isMainDataPath() ? "<mapping_input_specification>" : mapping.getOutputStepname() );
      } else {
        if ( mapping.isMainDataPath() ) {
          final List<TransHopMeta> hops = parentTransMeta.getTransHops();
          for ( final TransHopMeta hop : hops ) {
            if ( hop.getFromStep().equals( parentStepMeta ) ) {
              targetStep = hop.getToStep().getName();
              break;
            }
          }
        } else {
          targetStep = mapping.getOutputStepname();
        }
      }
    }
    return targetStep;
  }

  public void setCommonProps( final String mappingKey, final MappingIODefinition mapping,
                              final StringBuilder mappingStr, final List<String> verboseProps ) {
    rootNode.setProperty( mappingKey, mappingStr.toString() );
    final String updateFieldnamesKey = mappingKey + " update field names";
    verboseProps.add( updateFieldnamesKey );
    rootNode.setProperty( updateFieldnamesKey, mapping.isRenamingOnOutput() );

    int renameIdx = 1;
    for ( final MappingValueRename valueRename : mapping.getValueRenames() ) {
      final String renameKey = mappingKey + " rename [" + renameIdx++ + "]";
      verboseProps.add( renameKey );
      rootNode.setProperty( renameKey, valueRename.getSourceValueName() + " > " + valueRename.getTargetValueName() );
    }
  }
}

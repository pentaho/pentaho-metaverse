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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.ISubTransAwareMeta;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.mapping.MappingIODefinition;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.pentaho.di.trans.steps.mappinginput.MappingInputMeta;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransExtensionPointUtil;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.messages.Messages;
import org.pentaho.metaverse.util.MetaverseUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseMappingAnalyzer<T extends StepWithMappingMeta> extends StepAnalyzer<StepWithMappingMeta> {

  private static final Logger LOGGER = LogManager.getLogger( BaseMappingAnalyzer.class );

  @Override
  protected Set<StepField> getUsedFields( final StepWithMappingMeta meta ) {
    final Set<StepField> usedFields = new HashSet();
    return usedFields;
  }

  @Override
  protected void customAnalyze( final StepWithMappingMeta meta, final IMetaverseNode rootNode )
    throws MetaverseAnalyzerException {

    String transformationPath = parentTransMeta.environmentSubstitute( meta.getFileName() );
    try {
      transformationPath = KettleAnalyzerUtil.normalizeFilePath( transformationPath );
    } catch ( final MetaverseException e ) {
      // ignore
    }
    rootNode.setProperty( "subTransformation", transformationPath );

    final TransMeta subTransMeta = KettleAnalyzerUtil.getSubTransMeta( (ISubTransAwareMeta) meta );
    subTransMeta.setFilename( transformationPath );

    final IMetaverseNode subTransNode = getNode( subTransMeta.getName(), DictionaryConst.NODE_TYPE_TRANS,
      documentDescriptor.getNamespace(), null, null );
    subTransNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, documentDescriptor.getNamespace().getNamespaceId() );
    subTransNode.setProperty( DictionaryConst.PROPERTY_PATH,
      KettleAnalyzerUtil.getSubTransMetaPath( (ISubTransAwareMeta) meta, subTransMeta ) );
    subTransNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_EXECUTES, subTransNode );

    final String id = TransExtensionPointUtil.getFilename( subTransMeta );
    final IDocument subTransDocument = MetaverseUtil.buildDocument( metaverseBuilder, subTransMeta,
      id, documentDescriptor.getNamespace() );
    final IComponentDescriptor subtransDocumentDescriptor = new MetaverseComponentDescriptor(
      subTransDocument.getStringID(), DictionaryConst.NODE_TYPE_TRANS, documentDescriptor.getNamespace(),
      descriptor.getContext() );

    // analyze the sub-transformation
    documentAnalyzer.analyze( subtransDocumentDescriptor, subTransMeta, subTransNode,
      KettleAnalyzerUtil.getSubTransMetaPath( (ISubTransAwareMeta) meta, subTransMeta ) );
  }

  @Override
  public void postAnalyze( final StepWithMappingMeta meta )
    throws MetaverseAnalyzerException {

    final String transformationPath = parentTransMeta.environmentSubstitute( meta.getFileName() );

    final TransMeta subTransMeta = KettleAnalyzerUtil.getSubTransMeta( (ISubTransAwareMeta) meta );
    subTransMeta.setFilename( transformationPath );

    // get the vertex corresponding to this step
    final Vertex stepVertex = findStepVertex( parentTransMeta, parentStepMeta.getName() );

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
    processInputMappings( subTransMeta, stepVertex, meta.getInputMappings(), verboseProps );

    // ----------- analyze outputs, for each output:
    // - create a virtual step node for the mapping source step, the step name is either defined "mapping source
    //   step name, or if "main data path" is selected we need fake it since we will not have access to it, name it
    //   something like: [<sub-transformation>]: mapping output specification step name
    // - create a virtual step node for the output target step, the step name is defined in "output target step name",
    //   or if "main data path" is is the first output step encountered that the mapping step outputs into
    // - create virtual field nodes for all fields defined in the "fieldname from mapping step" column and
    //   "derives" links from these new virtual fields to the fields defined in the "fieldsname to target step" that
    //   belong to the output target step
    processOutputMappings( subTransMeta, stepVertex, meta.getOutputMappings(), verboseProps );

    // When "Update mapped field names" is selected, the name of the parent field is used, rather than the sub-trans
    // field and vice versa
    createLinks( subTransMeta, meta );

    // remove any output fields from this step vertex, as the mapping step should not have any output fields; fields
    // flow to target steps through the mapped steps within the sub-transformation
    final List<Vertex> outputFieldVertices = IteratorUtils.toList(
      stepVertex.getVertices( Direction.OUT, DictionaryConst.LINK_OUTPUTS ).iterator() );
    for ( final Vertex outputFieldVertex : outputFieldVertices ) {
      outputFieldVertex.remove();
    }
    setPropertySafely( stepVertex, DictionaryConst.PROPERTY_VERBOSE_DETAILS, StringUtils.join(
      verboseProps, "," ) );
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

  private static final String MAPPING_INPUT_SPEC = BaseMessages.getString(
    MappingInputMeta.class, "MappingInputDialog.Shell.Title" );

  private static final String MAPPING_OUTPUT_SPEC = BaseMessages.getString(
    MappingOutput.class, "MappingOutputDialog.Shell.Title" );

  private Vertex getInputSourceStepVertex( final TransMeta transMeta, final MappingIODefinition mapping ) {
    // get the vertex corresponding to the input source step; if "Main data path" is selected, this it the input
    // step (assuming there is only one) into this mapping step; if "Main data path" is not selected, it is the
    // step defined within the "Input source step name" field
    String stepName = null;
    if ( mapping != null ) {
      if ( mapping.isMainDataPath() ) {
        stepName = ArrayUtils.isEmpty( prevStepNames ) ? null : prevStepNames[ 0 ];
        LOGGER.warn( Messages.getString( "WARN.MultiplePreviousSteps", getStepName(), Arrays.toString( prevStepNames ),
          stepName ) );
      } else {
        // main path is not selected, therefore we can further refine our search by looking up the step by the name
        // defined in the "Mapping target step name" field
        stepName = mapping.getInputStepname();
      }
    }
    return findStepVertex( transMeta, stepName );
  }

  private Vertex getInputTargetStepVertex( final TransMeta transMeta, final MappingIODefinition mapping ) {
    // find the "Mapping input specification" step within the sub-transformation - there might be more than one,
    // if we have more than one "Mapping (sub-transformationn)" step within this ktr, so we need to find the one
    // connected to this specific sub-transformation
    final Map<String, String> propsLookupMap = new HashMap();
    propsLookupMap.put( DictionaryConst.PROPERTY_STEP_TYPE, MAPPING_INPUT_SPEC );
    if ( mapping != null && !mapping.isMainDataPath() ) {
      // main path is not selected, therefore we can further refine our search by looking up the step by the name
      // defined in the "Mapping target step name" field
      propsLookupMap.put( DictionaryConst.PROPERTY_NAME, mapping.getOutputStepname() );
    }
    return findStepVertex( transMeta, propsLookupMap );
  }

  private Vertex getOutputSourceStepVertex( final TransMeta transMeta, final MappingIODefinition mapping ) {
    // find the "Mapping output specification" step within the sub-transformation - there might be more than one,
    // if we have more than one "Mapping (sub-transformation)" step within this ktr, so we need to find the one
    // connected to this specific sub-transformation
    final Map<String, String> propsLookupMap = new HashMap();
    propsLookupMap.put( DictionaryConst.PROPERTY_STEP_TYPE, MAPPING_OUTPUT_SPEC );
    if ( mapping != null && !mapping.isMainDataPath() ) {
      // main path is not selected, therefore we can further refine our search by looking up the step by the name
      // defined in the "Mapping source step name" field
      propsLookupMap.put( DictionaryConst.PROPERTY_NAME, mapping.getInputStepname() );
    }
    return findStepVertex( transMeta, propsLookupMap );
  }

  private Vertex getOutputTargetStepVertex( final TransMeta transMeta, final MappingIODefinition mapping ) {
    // get the vertex corresponding to the output target step; if "Main data path" is selected, this it the output
    // step (assuming there is only one) from this mapping step; if "Main data path" is not selected, it is the
    // step defined within the "Output target step name" field
    final Map<String, String> propsLookupMap = new HashMap();
    propsLookupMap.put( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP );
    if ( mapping != null && !mapping.isMainDataPath() ) {
      // main path is not selected, therefore we can further refine our search by looking up the step by the name
      // defined in the "Mapping target step name" field
      propsLookupMap.put( DictionaryConst.PROPERTY_NAME, mapping.getOutputStepname() );
    } else {
      // we are using the main data path - that means we need to find the step that this step hops to
      final Vertex thisVertex = findStepVertex( parentTransMeta, parentStepMeta.getName() );
      final Iterator<Vertex> targetVertices = thisVertex.getVertices(
        Direction.OUT, DictionaryConst.LINK_HOPSTO ).iterator();
      while ( targetVertices.hasNext() ) {
        propsLookupMap.put( DictionaryConst.PROPERTY_NAME, targetVertices.next().getProperty( DictionaryConst
          .PROPERTY_NAME ).toString() );
        // we grab the first encountered target step
        break;
      }
    }
    return findStepVertex( transMeta, propsLookupMap );
  }

  private void createLinks( final TransMeta subTransMeta, final StepWithMappingMeta meta ) {
    boolean renameFields = shouldRenameFields( meta );

    final Map<String, String> fieldRenames = new HashMap();

    // process input mappings
    for ( final MappingIODefinition inputMapping : meta.getInputMappings() ) {

      final Vertex inputSourceVertex = getInputSourceStepVertex( parentTransMeta, inputMapping );
      final Vertex inputTargetVertex = getInputTargetStepVertex( subTransMeta, inputMapping );
      final String inputTargetName = inputTargetVertex.getProperty( DictionaryConst.PROPERTY_NAME );

      final List<MappingValueRename> renames = inputMapping.getValueRenames();
      for ( final MappingValueRename rename : renames ) {
        // This is deliberately target > source, since we are going to be looking up the target to rename back to the
        // source
        fieldRenames.put( rename.getTargetValueName(), rename.getSourceValueName() );
      }

      // traverse output fields of the input source step
      // for each field, if a rename exists, create a "derives" link from the field to the input target field with
      // the name defined in the "Fieldname to mapping input step" column; otherwise create a "derives" link to a
      // field with the same name
      final Iterator<Vertex> inputSourceOutputFields = inputSourceVertex.getVertices( Direction.OUT,
        DictionaryConst.LINK_OUTPUTS ).iterator();
      while ( inputSourceOutputFields.hasNext() ) {
        final Vertex inputSourceOutputField = inputSourceOutputFields.next();
        final String inputSourceOutputFieldName = inputSourceOutputField.getProperty( DictionaryConst.PROPERTY_NAME );
        // is there a rename for this field?
        final MappingValueRename renameMapping = inputMapping.getValueRenames().stream().filter( rename ->
          inputSourceOutputFieldName.equals( rename.getSourceValueName() ) ).findAny().orElse( null );
        // if there is no rename for this field, we look for a field with the same name, otherwise we look for a field
        // that is the target of the rename mapping ( defined within the "Fieldname to mapping input step" column);
        // we look for this field within the sub-transformation and within the context of the input target step
        final String derivedFieldName = renameMapping == null ? inputSourceOutputFieldName
          : renameMapping.getTargetValueName();
        final Vertex derivedField = findFieldVertex( subTransMeta, inputTargetName, derivedFieldName );
        // add an "inputs" link from the field of the input source step to the input target step
        metaverseBuilder.addLink( inputSourceOutputField, DictionaryConst.LINK_INPUTS, inputTargetVertex );
        // add a "derives" link from the field of the input source step to the output field of the input target step
        metaverseBuilder.addLink( inputSourceOutputField, DictionaryConst.LINK_DERIVES, derivedField );
      }
    }

    // process output mappings
    for ( final MappingIODefinition outputMapping : meta.getOutputMappings() ) {

      final Vertex outputSourceVertex = getOutputSourceStepVertex( subTransMeta, outputMapping );
      final Vertex outputTargetVertex = getOutputTargetStepVertex( parentTransMeta, outputMapping );
      final String outputTargetName = outputTargetVertex.getProperty( DictionaryConst.PROPERTY_NAME );

      // traverse output fields of the output source step
      // for each field, if a rename exists, create a "derives" link from the field to the input target field with
      // the name defined in the "Fieldname to mapping input step" column; otherwise create a "derives" link to a
      // field with the same name
      final Iterator<Vertex> outputSourceFields = outputSourceVertex.getVertices( Direction.OUT,
        DictionaryConst.LINK_OUTPUTS ).iterator();

      while ( outputSourceFields.hasNext() ) {
        final Vertex outputSourceField = outputSourceFields.next();
        final String outputSourceFieldName = outputSourceField.getProperty( DictionaryConst.PROPERTY_NAME );
        String derivedFieldName = outputSourceFieldName;
        // is there a rename mapping for this field?
        final MappingValueRename renameMapping = outputMapping.getValueRenames().stream().filter( rename ->
          outputSourceFieldName.equals( rename.getSourceValueName() ) ).findAny().orElse( null );
        // if an output rename mapping exists for this field, use the target field name for the derived field
        if ( renameMapping != null ) {
          derivedFieldName = renameMapping.getTargetValueName();
        } else if ( renameFields && fieldRenames.containsKey( outputSourceFieldName ) ) {
          // if no rename mapping exists, check if the field is defined within fieldRenamed
          derivedFieldName = fieldRenames.get( outputSourceFieldName );
        }
        final Vertex derivedField = findFieldVertex( parentTransMeta, outputTargetName, derivedFieldName );
        // add an "inputs" link from the field of the input source step to the input target step
        metaverseBuilder.addLink( outputSourceField, DictionaryConst.LINK_INPUTS, outputTargetVertex );
        // add a "derives" link from the field of the output source step to the output field of the output target step
        metaverseBuilder.addLink( outputSourceField, DictionaryConst.LINK_DERIVES, derivedField );

        // if the output target step has any virtual input steps that themselves aren't output by any step, remove
        // them; these are the orphaned output steps of the mapping step that the base analyzer code wasn't able to
        // resolve, and since the mapping step should not have any output fields, these can be removed
        final List<Vertex> allOutputTargetInputFields = IteratorUtils.toList(
          outputTargetVertex.getVertices( Direction.IN, DictionaryConst.LINK_INPUTS ).iterator() );
        for ( final Vertex outputTargetInputField : allOutputTargetInputFields ) {
          if ( "true".equals( outputTargetInputField.getProperty( DictionaryConst.NODE_VIRTUAL ).toString() ) ) {
            // check further that this field does not have a containing step that outputs it
            final Iterator<Vertex> parentSteps = outputTargetInputField.getVertices(
              Direction.IN, DictionaryConst.LINK_OUTPUTS ).iterator();
            boolean hasParent = false;
            while ( parentSteps.hasNext() ) {
              hasParent = true;
              break;
            }
            if ( !hasParent ) {
              outputTargetInputField.remove();
            }
          }
        }
      }
    }
  }

  private void processInputMappings( final TransMeta subTransMeta, final Vertex stepVertex,
                                     final List<MappingIODefinition> inputMappings, final List<String> verboseProps ) {
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
        setPropertySafely( stepVertex, descriptionKey, mapping.getDescription() );
      }
      setCommonProps( stepVertex, mappingKey, mapping, mappingStr, verboseProps );
      mappingIdx++;
    }
  }

  private void processOutputMappings( final TransMeta subTransMeta, final Vertex stepVertex,
                                      final List<MappingIODefinition> mappings, final List<String> verboseProps ) {
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
        setPropertySafely( stepVertex, descriptionKey, mapping.getDescription() );
      }
      setCommonProps( stepVertex, mappingKey, mapping, mappingStr, verboseProps );
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
        String sourceStepName = mapping.getInputStepname();
        if ( mapping.isMainDataPath() ) {
          final Vertex sourceVertex = getOutputSourceStepVertex( subTransMeta, mapping );
          sourceStepName = sourceVertex.getProperty( DictionaryConst.PROPERTY_NAME ).toString();
        }
        sourceStep = ( includeSubTransPrefix ? "[" + subTransMeta.getName() + "] " : "" ) + sourceStepName;
      }
    }
    return sourceStep;
  }

  private String getTargetStepName( final TransMeta subTransMeta, final MappingIODefinition mapping,
                                    final boolean includeSubTransPrefix, final boolean input ) {
    String targetStep = null;
    if ( mapping != null ) {
      if ( input ) {
        String targetStepName = mapping.getOutputStepname();
        if ( mapping.isMainDataPath() ) {
          final Vertex targetVertex = getInputTargetStepVertex( subTransMeta, mapping );
          targetStepName = targetVertex.getProperty( DictionaryConst.PROPERTY_NAME ).toString();
        }
        targetStep = ( includeSubTransPrefix ? "[" + subTransMeta.getName() + "] " : "" ) + targetStepName;
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

  public void setCommonProps( final Vertex stepVertex, final String mappingKey, final MappingIODefinition mapping,
                              final StringBuilder mappingStr, final List<String> verboseProps ) {
    setPropertySafely( stepVertex, mappingKey, mappingStr.toString() );
    final String updateFieldnamesKey = mappingKey + " update field names";
    verboseProps.add( updateFieldnamesKey );
    setPropertySafely( stepVertex, updateFieldnamesKey, Boolean.toString( mapping.isRenamingOnOutput() ) );

    int renameIdx = 1;
    for ( final MappingValueRename valueRename : mapping.getValueRenames() ) {
      final String renameKey = mappingKey + " rename [" + renameIdx++ + "]";
      verboseProps.add( renameKey );
      setPropertySafely( stepVertex, renameKey, valueRename.getSourceValueName() + " > " + valueRename
        .getTargetValueName() );
    }
  }
}

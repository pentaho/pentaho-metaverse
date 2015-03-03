/*!
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

package com.pentaho.metaverse.analyzer.kettle.step.selectvalues;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.ChangeType;
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;
import com.pentaho.metaverse.api.model.Operation;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;
import com.pentaho.metaverse.impl.model.kettle.FieldMapping;
import com.pentaho.metaverse.messages.Messages;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectMetadataChange;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The SelectValuesStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the fields
 * operated on by Select Values steps.
 */
public class SelectValuesStepAnalyzer extends BaseStepAnalyzer<SelectValuesMeta> {

  private static final Logger log = LoggerFactory.getLogger( SelectValuesStepAnalyzer.class );


  /**
   * This value is used by Select Values to indicate "no change" to a particular piece of metadata (precision, e.g.)
   */
  protected static final int NOT_CHANGED = -2;

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IAnalyzer#analyze(IComponentDescriptor,java.lang.Object)
   */
  @Override
  public IMetaverseNode analyze(
    IComponentDescriptor descriptor, SelectValuesMeta selectValuesMeta ) throws MetaverseAnalyzerException {

    // Do common analysis for all steps
    super.analyze( descriptor, selectValuesMeta );

    IMetaverseNode fieldNode;
    String inputFieldName;

    Set<ComponentDerivationRecord> changes = getChangeRecords( selectValuesMeta );
    for ( ComponentDerivationRecord change : changes ) {
      inputFieldName = change.getOriginalEntityName();
      fieldNode = createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, inputFieldName ) );
      // Get the ValueMetaInterface for the input field, to determine if any of its metadata has changed
      RowMetaInterface rowMetaInterface = ( prevFields == null ) ? new RowMeta() : prevFields.get( prevStepNames[0] );
      ValueMetaInterface inputFieldValueMeta = rowMetaInterface.searchValueMeta( inputFieldName );
      if ( inputFieldValueMeta == null ) {
        throw new MetaverseAnalyzerException( "Cannot determine type of field: " + inputFieldName );
      }
      IMetaverseNode newFieldNode = processFieldChangeRecord( descriptor, fieldNode, change );
      if ( newFieldNode != null ) {
        newFieldNode.setProperty( DictionaryConst.PROPERTY_KETTLE_TYPE, inputFieldValueMeta.getTypeDesc() );
        metaverseBuilder.addNode( newFieldNode );
        metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_CREATES, newFieldNode );
      }
      metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_USES, fieldNode );
    }
    return rootNode;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( SelectValuesMeta selectValuesMeta )
    throws MetaverseAnalyzerException {

    validateState( null, selectValuesMeta );
    if ( prevFields == null || stepFields == null ) {
      loadInputAndOutputStreamFields( selectValuesMeta );
    }
    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>();

    String inputFieldName;
    String outputFieldName;
    ComponentDerivationRecord changeRecord;

    // Process the fields/tabs in the same order as the real step does
    if ( !Const.isEmpty( selectValuesMeta.getSelectName() ) ) {
      String[] fieldNames = selectValuesMeta.getSelectName();
      String[] fieldRenames = selectValuesMeta.getSelectRename();
      int[] fieldLength = selectValuesMeta.getSelectLength();
      int[] fieldPrecision = selectValuesMeta.getSelectPrecision();

      for ( int i = 0; i < fieldNames.length; i++ ) {
        inputFieldName = fieldNames[i];
        outputFieldName = fieldRenames[i];

        changeRecord = new ComponentDerivationRecord( inputFieldName,
          outputFieldName == null ? inputFieldName : outputFieldName, ChangeType.METADATA );

        Set<String> metadataChangedFields = new HashSet<String>();

        if ( inputFieldName != null && outputFieldName != null && !inputFieldName.equals( outputFieldName ) ) {
          metadataChangedFields.add( "name" );
        }

        // Check for changes in field length
        if ( fieldLength != null && fieldLength[i] != NOT_CHANGED ) {
          metadataChangedFields.add( "length" );
        }

        // Check for changes in field precision
        if ( fieldPrecision != null && fieldPrecision[i] != NOT_CHANGED ) {
          metadataChangedFields.add( "precision" );
        }

        if ( !metadataChangedFields.isEmpty() ) {
          // Add all the changed metadata fields as a single operation
          changeRecord.addOperation(
            new Operation( DictionaryConst.PROPERTY_MODIFIED, StringUtils.join( metadataChangedFields, "," ) ) );
        }
        changeRecords.add( changeRecord );
      }
    }

    // No need to do the Remove tab, the SelectValuesMeta.getFields() will not include removed fields, and the
    // super.analyze() will notice and created a "deleted" relationship for each

    if ( !Const.isEmpty( selectValuesMeta.getMeta() ) ) {
      SelectMetadataChange[] metadataChanges = selectValuesMeta.getMeta();
      if ( metadataChanges != null ) {
        for ( SelectMetadataChange metadataChange : metadataChanges ) {
          inputFieldName = metadataChange.getName();
          outputFieldName = metadataChange.getRename();

          changeRecord = new ComponentDerivationRecord( inputFieldName,
            outputFieldName == null ? inputFieldName : outputFieldName, ChangeType.METADATA );

          Set<String> metadataChangedFields = new HashSet<String>();

          // NOTE: We use equalsIgnoreCase instead of equals because that's how Select Values currently works
          if ( inputFieldName != null && outputFieldName != null && !inputFieldName
            .equalsIgnoreCase( outputFieldName ) ) {
            metadataChangedFields.add( "name" );
          }

          // Get the ValueMetaInterface for the input field, to determine if any of its metadata has changed
          if ( prevFields == null ) {
            prevFields = getInputFields( selectValuesMeta );
            if ( prevFields == null ) {
              log.warn( Messages.getString( "WARNING.CannotDetermineFieldType", inputFieldName ) );
              continue;
            }
          }
          RowMetaInterface rowMetaInterface = prevFields.get( prevStepNames[0] );
          ValueMetaInterface inputFieldValueMeta = rowMetaInterface.searchValueMeta( inputFieldName );
          if ( inputFieldValueMeta == null ) {
            log.warn( Messages.getString( "WARNING.CannotDetermineFieldType", inputFieldName ) );
            continue;
          }

          // Check for changes in field type
          if ( inputFieldValueMeta.getType() != metadataChange.getType() ) {
            metadataChangedFields.add( "type" );
          }
          // Check for changes in field length
          if ( metadataChange.getLength() != NOT_CHANGED ) {
            metadataChangedFields.add( "length" );
          }
          // Check for changes in field precision
          if ( metadataChange.getPrecision() != NOT_CHANGED ) {
            metadataChangedFields.add( "precision" );
          }
          // Check for changes in storage type (binary to string, e.g.)
          if ( ( metadataChange.getStorageType() != -1 )
            && ( inputFieldValueMeta.getStorageType() != metadataChange.getStorageType() ) ) {
            metadataChangedFields.add( "storagetype" );
          }
          // Check for changes in conversion mask
          if ( ( metadataChange.getConversionMask() != null )
            && ( inputFieldValueMeta.getConversionMask() == null
            || !inputFieldValueMeta.getConversionMask().equals( metadataChange.getConversionMask() ) ) ) {
            metadataChangedFields.add( "conversionmask" );
          }
          // Check for changes in date format leniency
          if ( inputFieldValueMeta.isDateFormatLenient() != metadataChange.isDateFormatLenient() ) {
            metadataChangedFields.add( "dateformatlenient" );
          }
          // Check for changes in date format locale
          if ( ( metadataChange.getDateFormatLocale() != null )
            && ( inputFieldValueMeta.getDateFormatLocale() == null
            || !inputFieldValueMeta.getDateFormatLocale().toString()
            .equals( metadataChange.getDateFormatLocale() ) ) ) {
            metadataChangedFields.add( "datelocale" );
          }
          // Check for changes in date format locale
          if ( ( metadataChange.getDateFormatTimeZone() != null )
            && ( inputFieldValueMeta.getDateFormatTimeZone() == null
            || !inputFieldValueMeta.getDateFormatTimeZone().toString()
            .equals( metadataChange.getDateFormatTimeZone() ) ) ) {
            metadataChangedFields.add( "datetimezone" );
          }
          // Check for changes in date format locale
          if ( inputFieldValueMeta.isLenientStringToNumber() != metadataChange.isLenientStringToNumber() ) {
            metadataChangedFields.add( "lenientnumberconversion" );
          }
          // Check for changes in encoding
          if ( ( metadataChange.getDateFormatTimeZone() != null )
            && ( inputFieldValueMeta.getStringEncoding() == null
            || !inputFieldValueMeta.getStringEncoding().equals( metadataChange.getDateFormatTimeZone() ) ) ) {
            metadataChangedFields.add( "encoding" );
          }
          // Check for changes in encoding
          if ( ( metadataChange.getDecimalSymbol() != null )
            && ( inputFieldValueMeta.getDecimalSymbol() == null
            || !inputFieldValueMeta.getDecimalSymbol().equals( metadataChange.getDecimalSymbol() ) ) ) {
            metadataChangedFields.add( "decimalsymbol" );
          }
          // Check for changes in encoding
          if ( ( metadataChange.getGroupingSymbol() != null )
            && ( inputFieldValueMeta.getGroupingSymbol() == null
            || !inputFieldValueMeta.getGroupingSymbol().equals( metadataChange.getGroupingSymbol() ) ) ) {
            metadataChangedFields.add( "groupsymbol" );
          }
          // Check for changes in encoding
          if ( ( metadataChange.getCurrencySymbol() != null )
            && ( inputFieldValueMeta.getCurrencySymbol() == null
            || !inputFieldValueMeta.getCurrencySymbol().equals( metadataChange.getCurrencySymbol() ) ) ) {
            metadataChangedFields.add( "currencysymbol" );
          }

          if ( !metadataChangedFields.isEmpty() ) {
            // Add all the changed metadata fields as a single operation
            changeRecord.addOperation(
              new Operation( DictionaryConst.PROPERTY_MODIFIED, StringUtils.join( metadataChangedFields, "," ) ) );
          }
          changeRecords.add( changeRecord );
        }
      }
    }
    return changeRecords;
  }

  @Override
  public Set<IFieldMapping> getFieldMappings( SelectValuesMeta selectValuesMeta ) throws MetaverseAnalyzerException {
    validateState( null, selectValuesMeta );
    if ( prevFields == null || stepFields == null ) {
      loadInputAndOutputStreamFields( selectValuesMeta );
    }
    List<IFieldMapping> fieldMappings = new ArrayList<IFieldMapping>();

    // Would love to use selectValuesMeta.getFieldnameLineage() here,
    // but it only gives back info on fields that were renamed. We need all input fields to output fields

    String[] inputs = selectValuesMeta.getSelectName();
    String[] outputs = selectValuesMeta.getSelectRename();
    for ( int i = 0; i < inputs.length; i++ ) {
      String input = inputs[i];
      String output = outputs[i];
      FieldMapping fm = new FieldMapping( input, output );
      if ( Const.isEmpty( output ) ) {
        fm.setTargetFieldName( input );
      }
      fieldMappings.add( fm );
    }

    SelectMetadataChange[] metadataChanges = selectValuesMeta.getMeta();
    for ( int i = 0; i < metadataChanges.length; i++ ) {
      String input = metadataChanges[i].getName();
      String output = metadataChanges[i].getRename();
      if ( !Const.isEmpty( output ) ) {
        FieldMapping fm = new FieldMapping( input, output );
        for ( int j = 0; j < fieldMappings.size(); j++ ) {
          IFieldMapping fieldMapping = fieldMappings.get( j );
          if ( fieldMapping.getSourceFieldName().equalsIgnoreCase( input ) ) {
            fieldMappings.remove( fieldMapping );
            fieldMappings.add( j, fm );
            break;
          }
        }
      }
    }
    return new HashSet<IFieldMapping>( fieldMappings );
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( SelectValuesMeta.class );
      }
    };
  }
}

/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.step.selectvalues;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectMetadataChange;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.model.Operation;
import org.pentaho.metaverse.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The SelectValuesStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the fields
 * operated on by Select Values steps.
 */
public class SelectValuesStepAnalyzer extends StepAnalyzer<SelectValuesMeta> {

  private static final Logger log = LoggerFactory.getLogger( SelectValuesStepAnalyzer.class );

  /**
   * This value is used by Select Values to indicate "no change" to a particular piece of metadata (precision, e.g.)
   */
  protected static final int NOT_CHANGED = -2;

  @Override
  protected void customAnalyze( SelectValuesMeta meta, IMetaverseNode rootNode ) {
    // nothing custom to be done. The other overrides provide all that is needed
  }

  @Override
  protected boolean isPassthrough( StepField originalFieldName ) {
    // a field is considered a passthrough if:
    //   there are no entries on the select tab AND the field is NOT on the deleted tab OR meta tab
    if ( ArrayUtils.isEmpty( baseStepMeta.getSelectName() ) ) {
      SelectMetadataChange[] changes = baseStepMeta.getMeta();
      boolean isOnMetaTab = false;
      for ( int i = 0; i < changes.length; i++ ) {
        SelectMetadataChange change = changes[ i ];
        if ( originalFieldName.getFieldName().equalsIgnoreCase( change.getName() ) ) {
          isOnMetaTab = true;
          break;
        }
      }
      // check if the field is being deleted (make the check case insensitive)
      final List<String> fieldDeletesLowerCase = baseStepMeta.getDeleteName() == null ? new ArrayList<>() : Arrays
        .asList( baseStepMeta.getDeleteName() ).stream().map( String::toLowerCase ).collect( Collectors.toList() );
      boolean isDeleted = fieldDeletesLowerCase.contains( originalFieldName.getFieldName().toLowerCase() );
      return ( !( isOnMetaTab || isDeleted ) );
    }
    return false;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( SelectValuesMeta selectValuesMeta )
    throws MetaverseAnalyzerException {

    validateState( null, selectValuesMeta );
    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>();

    ComponentDerivationRecord changeRecord;
    // get a list of fields to be deleted, in lower case for case-insensitive comparisons
    final List<String> fieldDeletesLowerCase = selectValuesMeta.getDeleteName() == null ? new ArrayList<>() : Arrays
      .asList( selectValuesMeta.getDeleteName() ).stream().map( String::toLowerCase ).collect( Collectors.toList() );
    final SelectMetadataChange[] metadataChanges = selectValuesMeta.getMeta(); // Changes form the 'Meta-data' tab

    // Process the fields/tabs in the same order as the real step does
    if ( !Const.isEmpty( selectValuesMeta.getSelectName() ) ) {
      String[] fieldNames = selectValuesMeta.getSelectName();
      String[] fieldRenames = selectValuesMeta.getSelectRename(); // rename fields from the 'Select & Alter' tab
      int[] fieldLength = selectValuesMeta.getSelectLength();
      int[] fieldPrecision = selectValuesMeta.getSelectPrecision();

      for ( int i = 0; i < fieldNames.length; i++ ) {
        final String inputFieldName = fieldNames[i];
        final String outputFieldName = fieldRenames[i];
        // if the inputFieldName is being removed or renamed through the 'Select & Alter' tab or the 'Meta-data' tab,
        // DO NOT create a change record
        // Get a list of rename field names from the 'Meta-data' tab where the 'Fieldname' matches
        // (case-insensitive) inputFieldName, if the list is not empty, we know that inputFieldName is being renamed
        // via the 'Meta-data' tab and therefore we do not want to create a change record here, because it will be
        // addressed below, where the 'Meta-data' tab is being analyzed
        final List<String> metaRenameFieldsLowerCase = metadataChanges == null ? new ArrayList<>() : Arrays.asList(
          metadataChanges ).stream().filter( change -> change.getName().equalsIgnoreCase( inputFieldName ) ).map(
            e -> e.getRename() ).collect( Collectors.toList() );
        if ( StringUtils.isEmpty( outputFieldName ) && ( fieldDeletesLowerCase.contains( inputFieldName.toLowerCase() )
          || !CollectionUtils.isEmpty( metaRenameFieldsLowerCase ) ) ) {
          continue;
        }

        changeRecord = new ComponentDerivationRecord( inputFieldName, outputFieldName == null ? inputFieldName
          : outputFieldName, ChangeType.METADATA );

        final Set<String> metadataChangedFields = new HashSet<>();

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
      String[] prevStepNames = parentTransMeta.getPrevStepNames( getStepName() );
      if ( metadataChanges != null ) {
        for ( SelectMetadataChange metadataChange : metadataChanges ) {
          final String inputFieldName = metadataChange.getName();
          final String outputFieldName = metadataChange.getRename();
          // if the inputFieldName is being removed, DO NOT create a change record
          if ( StringUtils.isEmpty( outputFieldName ) && fieldDeletesLowerCase.contains(
            inputFieldName.toLowerCase() ) ) {
            continue;
          }

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
          ValueMetaInterface inputFieldValueMeta = null;
          if ( rowMetaInterface == null ) {
            log.warn( Messages.getString( "WARNING.CannotDetermineFieldType", inputFieldName ) );
            continue;
          }
          inputFieldValueMeta = rowMetaInterface.searchValueMeta( inputFieldName );
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
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> supported = new HashSet<>();
    supported.add( SelectValuesMeta.class );
    return supported;
  }

  @Override
  protected Set<StepField> getUsedFields( SelectValuesMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();

    String[] fieldNames = meta.getSelectName();
    for ( String fieldName : fieldNames ) {
      usedFields.addAll( createStepFields( fieldName, getInputs() ) );
    }

    SelectMetadataChange[] selectMetadataChanges = meta.getMeta();
    for ( SelectMetadataChange selectMetadataChange : selectMetadataChanges ) {
      usedFields.addAll( createStepFields( selectMetadataChange.getName(), getInputs() ) );
    }

    return usedFields;
  }

  ///// used for unit testing
  protected void setParentTransMeta( TransMeta parentTransMeta ) {
    this.parentTransMeta = parentTransMeta;
  }
  protected void setBaseStepMeta( SelectValuesMeta meta ) {
    this.baseStepMeta = meta;
  }

  @Override protected IClonableStepAnalyzer newInstance() {
    return new SelectValuesStepAnalyzer();
  }
}

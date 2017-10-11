/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.selectvalues;

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
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.model.Operation;
import org.pentaho.metaverse.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

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
        if ( originalFieldName.getFieldName().equals( change.getName() ) ) {
          isOnMetaTab = true;
          break;
        }
      }
      boolean isDeleted = ArrayUtils.contains( baseStepMeta.getDeleteName(), originalFieldName.getFieldName() );
      return ( !( isOnMetaTab || isDeleted ) );
    }
    return false;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( SelectValuesMeta selectValuesMeta )
    throws MetaverseAnalyzerException {

    validateState( null, selectValuesMeta );
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
      String[] prevStepNames = parentTransMeta.getPrevStepNames( getStepName() );
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
}

/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectMetadataChange;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.HashSet;
import java.util.Set;

/**
 * The SelectValuesStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the
 * fields operated on by Select Values steps.
 */
public class SelectValuesStepAnalyzer extends BaseStepAnalyzer<SelectValuesMeta> {

  /**
   * This value is used by Select Values to indicate "no change" to a particular piece of metadata (precision, e.g.)
   */
  protected static final int NOT_CHANGED = -2;

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IAnalyzer#analyze(IMetaverseComponentDescriptor,java.lang.Object)
   */
  @Override
  public IMetaverseNode analyze(
      IMetaverseComponentDescriptor descriptor, SelectValuesMeta selectValuesMeta ) throws MetaverseAnalyzerException {

    // Do common analysis for all steps
    IMetaverseNode node = super.analyze( descriptor, selectValuesMeta );

    IMetaverseNode fieldNode = null;
    String inputFieldName = null;
    String outputFieldName = null;
    ComponentDerivationRecord changeRecord = null;

    // Process the fields/tabs in the same order as the real step does
    if ( !Const.isEmpty( selectValuesMeta.getSelectName() ) ) {
      String[] fieldNames = selectValuesMeta.getSelectName();
      String[] fieldRenames = selectValuesMeta.getSelectRename();
      int[] fieldLength = selectValuesMeta.getSelectLength();
      int[] fieldPrecision = selectValuesMeta.getSelectPrecision();

      for ( int i = 0; i < fieldNames.length; i++ ) {
        inputFieldName = fieldNames[i];
        outputFieldName = fieldRenames[i];

        // We can't use our own descriptor here, we need to get the descriptor for the origin step
        fieldNode =
            createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, inputFieldName ) );

        changeRecord = new ComponentDerivationRecord( inputFieldName );

        // NOTE: We use equalsIgnoreCase instead of equals because that's how Select Values currently works
        if ( inputFieldName != null && outputFieldName != null && !inputFieldName
            .equalsIgnoreCase( outputFieldName ) ) {
          changeRecord.setEntityName( outputFieldName );
          changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "name" );
        }

        // Check for changes in field length
        if ( fieldLength != null && fieldLength[i] != NOT_CHANGED ) {
          changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "length" );
        }

        // Check for changes in field precision
        if ( fieldPrecision != null && fieldPrecision[i] != NOT_CHANGED ) {
          changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "precision" );
        }

        // Get the ValueMetaInterface for the input field, to determine if any of its metadata has changed
        ValueMetaInterface inputFieldValueMeta = prevFields.searchValueMeta( inputFieldName );
        if ( inputFieldValueMeta == null ) {
          throw new MetaverseAnalyzerException( "Cannot determine type of field: " + inputFieldName );
        }

        IMetaverseNode newFieldNode = processFieldChangeRecord( descriptor, fieldNode, changeRecord );
        if ( newFieldNode != null ) {
          newFieldNode.setProperty( "kettleType", inputFieldValueMeta.getTypeDesc() );
          metaverseBuilder.addNode( newFieldNode );
          metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_CREATES, newFieldNode );
        }
      }
    }

    // No need to do the Remove tab, the SelectValuesMeta.getFields() will not include removed fields, and the
    // super.analyze() will notice and created a "deleted" relationship for each

    if ( !Const.isEmpty( selectValuesMeta.getMeta() ) ) {
      SelectMetadataChange[] metadataChanges = selectValuesMeta.getMeta();
      if ( metadataChanges != null ) {
        for ( SelectMetadataChange metadataChange : metadataChanges ) {
          inputFieldName = metadataChange.getName();
          // We can't use our own descriptor here, we need to get the descriptor for the origin step
          fieldNode =
              createNodeFromDescriptor( getPrevStepFieldOriginDescriptor( descriptor, inputFieldName ) );

          changeRecord = new ComponentDerivationRecord( inputFieldName );

          outputFieldName = metadataChange.getRename();

          // NOTE: We use equalsIgnoreCase instead of equals because that's how Select Values currently works
          if ( inputFieldName != null && outputFieldName != null && !inputFieldName
              .equalsIgnoreCase( outputFieldName ) ) {
            changeRecord.setEntityName( outputFieldName );
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "name" );
          }

          // Get the ValueMetaInterface for the input field, to determine if any of its metadata has changed
          ValueMetaInterface inputFieldValueMeta = prevFields.searchValueMeta( inputFieldName );
          if ( inputFieldValueMeta == null ) {
            throw new MetaverseAnalyzerException( "Cannot determine type of field: " + inputFieldName );
          }
          String kettleType = inputFieldValueMeta.getTypeDesc();

          // Check for changes in field type
          if ( inputFieldValueMeta.getType() != metadataChange.getType() ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "type" );
            kettleType = ValueMetaFactory.getValueMetaName( metadataChange.getType() );
          }
          // Check for changes in field length
          if ( metadataChange.getLength() != NOT_CHANGED ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "length" );
          }
          // Check for changes in field precision
          if ( metadataChange.getPrecision() != NOT_CHANGED ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "precision" );
          }
          // Check for changes in storage type (binary to string, e.g.)
          if ( ( metadataChange.getStorageType() != -1 )
              && ( inputFieldValueMeta.getStorageType() != metadataChange.getStorageType() ) ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "storagetype" );
          }
          // Check for changes in conversion mask
          if ( ( metadataChange.getConversionMask() != null )
              && ( !inputFieldValueMeta.getConversionMask().equals( metadataChange.getConversionMask() ) ) ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "conversionmask" );
          }
          // Check for changes in date format leniency
          if ( inputFieldValueMeta.isDateFormatLenient() != metadataChange.isDateFormatLenient() ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "dateformatlenient" );
          }
          // Check for changes in date format locale
          if ( ( metadataChange.getDateFormatLocale() != null )
              && ( !inputFieldValueMeta.getDateFormatLocale().equals( metadataChange.getDateFormatLocale() ) ) ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "datelocale" );
          }
          // Check for changes in date format locale
          if ( ( metadataChange.getDateFormatTimeZone() != null )
              && ( !inputFieldValueMeta.getDateFormatTimeZone().equals( metadataChange.getDateFormatTimeZone() ) ) ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "datetimezone" );
          }
          // Check for changes in date format locale
          if ( inputFieldValueMeta.isLenientStringToNumber() != metadataChange.isLenientStringToNumber() ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "lenientnumberconversion" );
          }
          // Check for changes in encoding
          if ( ( metadataChange.getDateFormatTimeZone() != null )
              && ( inputFieldValueMeta.getStringEncoding() == null
              ||  !inputFieldValueMeta.getStringEncoding().equals( metadataChange.getDateFormatTimeZone() ) ) ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "encoding" );
          }
          // Check for changes in encoding
          if ( ( metadataChange.getDecimalSymbol() != null )
              && ( inputFieldValueMeta.getDecimalSymbol() == null
              || !inputFieldValueMeta.getDecimalSymbol().equals( metadataChange.getDecimalSymbol() ) ) ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "decimalsymbol" );
          }
          // Check for changes in encoding
          if ( ( metadataChange.getGroupingSymbol() != null )
              && ( inputFieldValueMeta.getGroupingSymbol() == null
              || !inputFieldValueMeta.getGroupingSymbol().equals( metadataChange.getGroupingSymbol() ) ) ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "groupsymbol" );
          }
          // Check for changes in encoding
          if ( ( metadataChange.getCurrencySymbol() != null )
              && ( inputFieldValueMeta.getCurrencySymbol() == null
              || !inputFieldValueMeta.getCurrencySymbol().equals( metadataChange.getCurrencySymbol() ) ) ) {
            changeRecord.addOperand( DictionaryConst.PROPERTY_MODIFIED, "currencysymbol" );
          }

          IMetaverseNode newFieldNode = processFieldChangeRecord( descriptor, fieldNode, changeRecord );
          if ( newFieldNode != null ) {
            newFieldNode.setProperty( "kettleType", inputFieldValueMeta.getTypeDesc() );
            metaverseBuilder.addNode( newFieldNode );
            metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_CREATES, newFieldNode );
          }
        }
      }
    }

    return rootNode;
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

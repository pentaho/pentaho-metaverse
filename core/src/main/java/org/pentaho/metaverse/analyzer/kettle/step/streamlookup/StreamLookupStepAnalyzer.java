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

package org.pentaho.metaverse.analyzer.kettle.step.streamlookup;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.streamlookup.StreamLookupMeta;
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
import java.util.Map;
import java.util.Set;


public class StreamLookupStepAnalyzer extends StepAnalyzer<StreamLookupMeta> {

  private static final Logger LOGGER = LoggerFactory.getLogger( StepAnalyzer.class );

  protected String[] keyLookups;
  protected String[] keyStreams;
  protected String[] values;
  protected String[] valueNames;
  protected String lookupStep;

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> set = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    set.add( StreamLookupMeta.class );
    return set;
  }

  @Override
  public Map<String, RowMetaInterface> getInputFields( StreamLookupMeta meta ) {
    Map<String, RowMetaInterface> rowMeta = super.getInputFields( meta );

    if ( parentTransMeta != null ) {
      for ( String prevStepName : parentTransMeta.getPrevStepNames( parentStepMeta ) ) {
        if ( !rowMeta.containsKey( prevStepName ) ) {
          try {
            rowMeta.put( prevStepName, parentTransMeta.getStepFields( prevStepName ) );
          } catch ( KettleStepException e ) {
            LOGGER.warn( Messages.getString( "WARNING.CannotDetermineRowMeta", prevStepName, e.toString() ) );
          }
        }
      }
    }
    return rowMeta;
  }

  @Override
  protected Set<StepField> getUsedFields( StreamLookupMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();
    keyLookups = meta.getKeylookup();
    keyStreams = meta.getKeystream();
    values = meta.getValue();

    String[] prevStepNames = parentTransMeta.getPrevStepNames( getStepName() );
    lookupStep = meta.getStepIOMeta().getInfoStreams().get( 0 ).getStepname();

    for ( int i = 0; i < keyLookups.length; i++ ) {
      String leftField = keyStreams[i];
      String rightField = keyLookups[i];

      usedFields.add( new StepField( prevStepNames[0], leftField ) );
      usedFields.add( new StepField( lookupStep, rightField ) );
    }
    for ( String retrieveField : values ) {
      usedFields.add( new StepField( lookupStep, retrieveField ) );
    }

    return usedFields;
  }

  @Override
  protected void customAnalyze( StreamLookupMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    keyLookups = meta.getKeylookup();
    keyStreams = meta.getKeystream();
    values = meta.getValue();
    valueNames = meta.getValueName();
    lookupStep = meta.getStepIOMeta().getInfoStreams().get( 0 ).getStepname();
    String[] prevStepNames = parentTransMeta.getPrevStepNames( getStepName() );

    for ( int i = 0; i < keyLookups.length; i++ ) {
      IMetaverseNode keyNode = getInputs().findNode( prevStepNames[0], keyStreams[i] );
      IMetaverseNode keyLookupNode = getInputs().findNode( lookupStep, keyLookups[i] );
      // Bidirectional Join
      metaverseBuilder.addLink( keyLookupNode, DictionaryConst.LINK_JOINS, keyNode );
      metaverseBuilder.addLink( keyNode, DictionaryConst.LINK_JOINS, keyLookupNode );
    }
  }

  /**
   * Identify the name collision renames and add change records for them.
   * <p/>
   * example: join fields in both input steps named COUNTRY. the second (right) field gets renamed with a suffix
   * on the way out of the step. You end up with COUNTRY (from the left) & COUNTRY_1 (from the right)
   *
   * @param meta
   * @return
   * @throws MetaverseAnalyzerException
   */
  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( StreamLookupMeta meta ) throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changeRecords = new HashSet<>();

    String[] names = meta.getValue();
    String[] newFields = meta.getValueName();

    for ( int i = 0; i < names.length; i++ ) {
      String name = names[i];
      String newFieldName = newFields[i];

      if ( newFieldNameExistsInMainInputStream( newFieldName ) ) {
        // the new field name is going to be renamed with the _N naming convention to make it unique
        RowMetaInterface outputFields = getOutputFields( meta );
        for ( int renameIdx = 1; renameIdx <= valueNames.length; renameIdx++ ) {
          ValueMetaInterface outField = outputFields.searchValueMeta( newFieldName + "_" + renameIdx );
          if ( outField == null ) {
            newFieldName = newFieldName + "_" + renameIdx;
            break;
          }
        }
      }
      ComponentDerivationRecord renameFieldRecord = new ComponentDerivationRecord(
        name, newFieldName, ChangeType.METADATA );
      renameFieldRecord.addOperation( Operation.getRenameOperation() );
      changeRecords.add( renameFieldRecord );
    }

    return changeRecords;
  }

  protected boolean newFieldNameExistsInMainInputStream( String newFieldName ) {
    if ( parentTransMeta != null ) {
      String lookupStepName = baseStepMeta.getStepIOMeta().getInfoStreams().get( 0 ).getStepname();
      String[] prevStepNames = parentTransMeta.getPrevStepNames( parentStepMeta );
      for ( int i = 0; i < prevStepNames.length; i++ ) {
        String prevStepName = prevStepNames[ i ];
        if ( !prevStepName.equals( lookupStepName ) ) {
          try {
            RowMetaInterface stepFields = parentTransMeta.getStepFields( prevStepName );
            if ( stepFields != null ) {
              ValueMetaInterface valueMetaInterface = stepFields.searchValueMeta( newFieldName );
              return valueMetaInterface != null;
            }
          } catch ( KettleStepException e ) {
            LOGGER.warn( Messages.getString( "WARNING.CannotDetermineRowMeta", prevStepName, e.toString() ) );
          }
        }
      }
    }
    return false;
  }

  // ******** Start - Used to aid in unit testing **********
  protected void setStepMeta( StreamLookupMeta meta ) {
    this.baseStepMeta = meta;
  }
  protected void setParentTransMeta( TransMeta parentTransMeta ) {
    this.parentTransMeta = parentTransMeta;
  }
  protected void setParentStepMeta( StepMeta parentStepMeta ) {
    this.parentStepMeta = parentStepMeta;
  }
  // ******** End - Used to aid in unit testing **********

  /**
   * Stream Lookup is a special sten, only take into account the first input step
   * @param rowMeta
   * @param rmi
   */
  protected void populateInputFieldsRowMeta( final Map<String, RowMetaInterface> rowMeta, final RowMetaInterface rmi ) {
    rowMeta.put( prevStepNames[ 0 ], rmi );
  }


}

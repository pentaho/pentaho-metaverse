/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.mergejoin;

import org.pentaho.di.core.ProgressNullMonitorListener;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.model.Operation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The MergeJoinStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and
 * other metaverse entities
 */
public class MergeJoinStepAnalyzer extends StepAnalyzer<MergeJoinMeta> {

  protected RowMetaInterface leftStepFields;
  protected RowMetaInterface rightStepFields;

  @Override
  protected void customAnalyze( MergeJoinMeta mergeJoinMeta, IMetaverseNode node ) throws MetaverseAnalyzerException {

    // create links for the merging of the input streams on field(s)
    String[] keyFields1 = mergeJoinMeta.getKeyFields1();
    String[] keyFields2 = mergeJoinMeta.getKeyFields2();
    String joinType = mergeJoinMeta.getJoinType();
    String[] prevStepNames = parentTransMeta.getPrevStepNames( getStepName() );

    node.setProperty( DictionaryConst.PROPERTY_JOIN_TYPE, joinType );
    node.setProperty( DictionaryConst.PROPERTY_JOIN_FIELDS_LEFT, Arrays.asList( keyFields1 ) );
    node.setProperty( DictionaryConst.PROPERTY_JOIN_FIELDS_RIGHT, Arrays.asList( keyFields2 ) );

    boolean isInner = MergeJoinMeta.join_types[0].equals( joinType );
    boolean isLeftOuter = MergeJoinMeta.join_types[1].equals( joinType );
    boolean isRightOuter = MergeJoinMeta.join_types[2].equals( joinType );
    boolean isFullOuter = MergeJoinMeta.join_types[3].equals( joinType );

    for ( int i = 0; i < keyFields1.length; i++ ) {
      IMetaverseNode leftNode = getInputs().findNode( prevStepNames[ 0 ], keyFields1[ i ] );
      IMetaverseNode rightNode = getInputs().findNode( prevStepNames[ 1 ], keyFields2[ i ] );

      // handle links for join types between fields
      if ( ( isInner || isLeftOuter || isFullOuter ) && leftNode != null && rightNode != null ) {
        metaverseBuilder.addLink( leftNode, DictionaryConst.LINK_JOINS, rightNode );
      }
      if ( ( isInner || isRightOuter || isFullOuter ) && leftNode != null && rightNode != null ) {
        metaverseBuilder.addLink( rightNode, DictionaryConst.LINK_JOINS, leftNode );
      }
    }

  }

  /**
   * Identify the name collision renames and add change records for them.
   *
   * example: join fields in both input steps named COUNTRY. the second (right) field gets renamed with a suffix
   * on the way out of the step. You end up with COUNTRY (from the left) & COUNTRY_1 (from the right)
   *
   * @param meta
   * @return
   * @throws MetaverseAnalyzerException
   */
  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( MergeJoinMeta meta ) throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changeRecords = new HashSet<>();
    String[] prevStepNames = parentTransMeta.getPrevStepNames( getStepName() );
    if ( getOutputs() != null ) {
      Set<StepField> outputFields = getOutputs().getFieldNames();

      for ( StepField outputField : outputFields ) {
        if ( outputField.getFieldName().matches( ".*_\\d*$" ) ) {
          String unsuffixName = outputField.getFieldName().replaceAll( "_\\d*$", "" );

          StepField rightSideInputField = new StepField( prevStepNames[ 1 ], unsuffixName );
          if ( !isPassthrough( rightSideInputField ) ) {
            ComponentDerivationRecord renameFieldRecord = new ComponentDerivationRecord(
              rightSideInputField, outputField, ChangeType.METADATA );
            renameFieldRecord.addOperation( Operation.getRenameOperation() );
            changeRecords.add( renameFieldRecord );
          }
        }
      }
    }

    return changeRecords;
  }

  @Override
  protected Set<StepField> getUsedFields( MergeJoinMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();
    String[] keyFields1 = meta.getKeyFields1();
    String[] keyFields2 = meta.getKeyFields2();

    String[] prevStepNames = parentTransMeta.getPrevStepNames( getStepName() );

    for ( int i = 0; i < keyFields1.length; i++ ) {
      String leftField = keyFields1[ i ];
      String rightField = keyFields2[ i ];

      usedFields.add( new StepField( prevStepNames[0], leftField ) );
      usedFields.add( new StepField( prevStepNames[1], rightField ) );
    }

    return usedFields;
  }

  @Override
  public Map<String, RowMetaInterface> getInputFields( MergeJoinMeta meta ) {
    Map<String, RowMetaInterface> rowMeta = null;
    try {
      validateState( null, meta );
    } catch ( MetaverseAnalyzerException e ) {
      // eat it
    }
    if ( parentTransMeta != null ) {
      rowMeta = new HashMap<>();
      try {
        StepMeta stepMeta1 = meta.getStepIOMeta().getInfoStreams().get( 0 ).getStepMeta();
        ProgressNullMonitorListener progress = new ProgressNullMonitorListener();
        leftStepFields = parentTransMeta.getStepFields( stepMeta1, progress );
        progress.done();

        progress = new ProgressNullMonitorListener();
        StepMeta stepMeta2 = meta.getStepIOMeta().getInfoStreams().get( 1 ).getStepMeta();
        rightStepFields = parentTransMeta.getStepFields( stepMeta2, progress );
        progress.done();
        rowMeta.put( stepMeta1.getName(), leftStepFields );
        rowMeta.put( stepMeta2.getName(), rightStepFields );

      } catch ( Throwable t ) {
        // eat it
      }
    }
    return rowMeta;
  }

  @Override
  protected boolean isPassthrough( StepField originalField ) {
    List<IMetaverseNode> inputFieldNamesMatching = getInputs().findNodes( originalField.getFieldName() );
    String[] prevStepNames = parentTransMeta.getPrevStepNames( getStepName() );

    boolean isRightSideOfJoin = originalField.getStepName().equals( prevStepNames[ 1 ] );
    boolean isBothSidesOfJoin = inputFieldNamesMatching.size() > 1;

    // if 2 fields coming in have the same name, the one from the 'right' side of the join will be renamed.
    // it is not a passthrough
    if ( !isRightSideOfJoin ) {
      // all fields on the left side are passthrough fields
      return true;
    } else if ( isRightSideOfJoin && !isBothSidesOfJoin ) {
      return true;
    }
    return false;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> set = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    set.add( MergeJoinMeta.class );
    return set;
  }

  // ******** Start - Used to aid in unit testing **********
  protected void setParentTransMeta( TransMeta parent ) {
    parentTransMeta = parent;
  }

  protected void setParentStepMeta( StepMeta parent ) {
    parentStepMeta = parent;
  }
  // ******** End - Used to aid in unit testing **********

}

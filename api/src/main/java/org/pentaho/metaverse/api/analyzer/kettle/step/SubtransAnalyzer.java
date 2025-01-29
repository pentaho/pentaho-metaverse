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

package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.apache.commons.collections4.CollectionUtils;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.rowsfromresult.RowsFromResultMeta;
import org.pentaho.di.trans.steps.rowstoresult.RowsToResultMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.messages.Messages;
import org.pentaho.metaverse.api.model.BaseMetaverseBuilder;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Predicate;

public class SubtransAnalyzer<T extends BaseStepMeta> {
  private StepAnalyzer<T> stepAnalyzer;
  private Logger log;

  public SubtransAnalyzer( StepAnalyzer<T> stepAnalyzer, Logger log ) {
    this.stepAnalyzer = stepAnalyzer;
    this.log = log;
  }

  public void linkUsedFieldToSubTrans( IMetaverseNode originalFieldNode, TransMeta subTransMeta,
                                       IMetaverseNode subTransNode, IComponentDescriptor descriptor ) {
    linkUsedFieldToSubTrans( originalFieldNode, subTransMeta, subTransNode, descriptor,
      fieldName -> originalFieldNode.getName().equals( fieldName ) );
  }

  /**
   * Checks to see if the sub trans has any RowFromResult steps in it.
   * If so, it will link the original field node to the fields created in the RowFromResult step in the sub trans
   * @param originalFieldNode incoming stream field node to the TransExecutorStep
   * @param subTransMeta TransMeta of the transformation to be executed by the TransExecutor step
   * @param subTransNode IMetaverseNode representing the sub-transformation to be executed
   * @param descriptor Descriptor to use as a basis
   * @param fieldPredicate predicate to determine if this is the sub field to link with
   */
  public void linkUsedFieldToSubTrans( IMetaverseNode originalFieldNode, TransMeta subTransMeta,
                                       IMetaverseNode subTransNode, IComponentDescriptor descriptor,
                                       Predicate<String> fieldPredicate ) {

    List<StepMeta> steps = subTransMeta.getSteps();
    if ( !CollectionUtils.isEmpty( steps ) ) {
      for ( StepMeta step : steps ) {
        if ( step.getStepMetaInterface() instanceof RowsFromResultMeta ) {
          RowsFromResultMeta rfrm = (RowsFromResultMeta) step.getStepMetaInterface();

          // Create a new descriptor for the RowsFromResult step.
          IComponentDescriptor stepDescriptor = new MetaverseComponentDescriptor( StepAnalyzer.NONE,
                  DictionaryConst.NODE_TYPE_TRANS_STEP, subTransNode, descriptor.getContext() );

          // Create a new node for the step, to be used as the parent of the the field we want to link to
          IMetaverseNode subTransStepNode = stepAnalyzer.createNodeFromDescriptor( stepDescriptor );

          if ( linkUsedFieldToStepField( originalFieldNode, subTransMeta, descriptor, fieldPredicate, step, rfrm,
            subTransStepNode ) ) {
            return;
          }
        }
      }
    }
  }

  /**
   * Checks to see if the sub trans has any RowFromResult steps in it.
   * If so, it will link the original field node to the fields created in the RowFromResult step in the sub trans
   * @param originalFieldNode incoming stream field node to the TransExecutorStep
   * @param subTransMeta TransMeta of the transformation to be executed by the TransExecutor step
   * @param descriptor Descriptor to use as a basis
   * @param fieldPredicate predicate to determine if this is the sub field to link with
   * @param step StepMeta of the step in the subtrans to link to
   * @param rfrm RowsFromResultMeta of the step to link to
   * @param subTransStepNode IMetaverseNode representing the step in the subtrans to link to
   * @return true if a field matching the fieldPredicate was found in the subtrans, false otherwise
   */
  private boolean linkUsedFieldToStepField( IMetaverseNode originalFieldNode, TransMeta subTransMeta,
                                            IComponentDescriptor descriptor, Predicate<String> fieldPredicate,
                                            StepMeta step, RowsFromResultMeta rfrm, IMetaverseNode subTransStepNode ) {
    try {
      RowMetaInterface rowMetaInterface = rfrm.getParentStepMeta().getParentTransMeta().getStepFields( step );
      for ( int i = 0; i < rowMetaInterface.getFieldNames().length; i++ ) {
        String field = rowMetaInterface.getFieldNames()[ i ];
        if ( fieldPredicate.test( field ) ) {
          // Create the descriptor for the trans field that is derived from the incoming result field
          IComponentDescriptor stepFieldDescriptor = new MetaverseComponentDescriptor( field,
                  DictionaryConst.NODE_TYPE_TRANS_FIELD, subTransStepNode, descriptor.getContext() );

          // Create the node
          IMetaverseNode subTransField =
                  stepAnalyzer.createFieldNode( stepFieldDescriptor, rowMetaInterface.getValueMeta( i ), step.getName(), false );

          // Add the link
          stepAnalyzer.getMetaverseBuilder().addLink( originalFieldNode, DictionaryConst.LINK_DERIVES, subTransField );

          // no need to keep looking for a match on field name, we just handled it.
          return true;
        }
      }
    } catch ( KettleStepException e ) {
      log.warn( Messages.getString( "WARN.SubtransAnalyzer.RowsFromResultNotFound", subTransMeta.getName() ),
              e );
    }
    return false;
  }

  /**
   * Checks to see if the sub trans has any RowToResult steps in it.
   * If so, it will link the fields it outputs to the fields created by this step and are sent to the
   * "target step for output rows".
   *
   * @param streamFieldNode stream field node sent to the step defined as "target step for output rows"
   * @param subTransMeta TransMeta of the transformation to be executed by the TransExecutor step
   * @param subTransNode IMetaverseNode representing the sub-transformation to be executed
   * @param descriptor Descriptor to use as a basis for any nodes created
   */
  public void linkResultFieldToSubTrans( IMetaverseNode streamFieldNode, TransMeta subTransMeta,
                                         IMetaverseNode subTransNode, IComponentDescriptor descriptor ) {

    linkResultFieldToSubTrans( streamFieldNode, subTransMeta, subTransNode, descriptor, null );
  }

  /**
   * Checks to see if the sub trans has the specified step name in it.
   * If so, it will link the fields it outputs to the fields created by this step and are sent to the
   * "target step for output rows".
   *
   * @param streamFieldNode stream field node sent to the step defined as "target step for output rows"
   * @param subTransMeta TransMeta of the transformation to be executed by the TransExecutor step
   * @param subTransNode IMetaverseNode representing the sub-transformation to be executed
   * @param descriptor Descriptor to use as a basis for any nodes created
   * @param resultStepName Name of step whose outputs need to connect to parent trans/step
   */
  public void linkResultFieldToSubTrans( IMetaverseNode streamFieldNode, TransMeta subTransMeta,
                                         IMetaverseNode subTransNode, IComponentDescriptor descriptor,
                                         String resultStepName ) {

    List<StepMeta> steps = subTransMeta.getSteps();
    if ( !CollectionUtils.isEmpty( steps ) ) {
      for ( StepMeta step : steps ) {
        // either look for the step with the specified name, or the name was null and we want the Rows to Result step
        if ( ( ( null != resultStepName ) && step.getName().equals( resultStepName ) )
          || ( null == resultStepName ) && step.getStepMetaInterface() instanceof RowsToResultMeta ) {

          BaseStepMeta baseStepMeta = (BaseStepMeta) step.getStepMetaInterface();

          // Create a new descriptor for the RowsToResult step.
          IComponentDescriptor stepDescriptor = new MetaverseComponentDescriptor( step.getName(),
                  DictionaryConst.NODE_TYPE_TRANS_STEP, subTransNode, descriptor.getContext() );

          // Create a new node for the step, to be used as the parent of the the field we want to link to
          IMetaverseNode subTransStepNode = stepAnalyzer.createNodeFromDescriptor( stepDescriptor );

          linkResultFieldToStepField( streamFieldNode, subTransMeta, descriptor, resultStepName, step, baseStepMeta,
            subTransStepNode );

        }
      }
    }

  }

  /**
   * Checks to see if the sub trans has the specified step name in it.
   * If so, it will link the fields it outputs to the fields created by this step and are sent to the
   * "target step for output rows".
   *
   * @param streamFieldNode stream field node sent to the step defined as "target step for output rows"
   * @param subTransMeta TransMeta of the transformation to be executed by the TransExecutor step
   * @param descriptor Descriptor to use as a basis for any nodes created
   * @param resultStepName Name of step whose outputs need to connect to parent trans/step
   * @param step StepMeta of the step in the subtrans to link to
   * @param baseStepMeta BaseStepMeta of the field in the subtrans to link to
   * @param subTransStepNode IMetaverseNode representing the step in the subtrans to link to
   */
  private void linkResultFieldToStepField( IMetaverseNode streamFieldNode, TransMeta subTransMeta,
                                           IComponentDescriptor descriptor, String resultStepName, StepMeta step,
                                           BaseStepMeta baseStepMeta, IMetaverseNode subTransStepNode ) {
    try {
      RowMetaInterface rowMetaInterface = baseStepMeta.getParentStepMeta().getParentTransMeta().getStepFields( step );
      for ( int i = 0; i < rowMetaInterface.getFieldNames().length; i++ ) {
        String field = rowMetaInterface.getFieldNames()[ i ];
        if ( streamFieldNode.getName().equals( field ) ) {
          // Create the descriptor for the trans field that is derived from the incoming result field
          IComponentDescriptor stepFieldDescriptor = new MetaverseComponentDescriptor( field,
                  DictionaryConst.NODE_TYPE_TRANS_FIELD, subTransStepNode, descriptor.getContext() );

          ValueMetaInterface fieldValueMeta = rowMetaInterface.getValueMeta( i );

          IMetaverseNode subTransField =
            getNodeForField( subTransMeta, resultStepName, step, fieldValueMeta, stepFieldDescriptor );

          // Add the link
          stepAnalyzer.getMetaverseBuilder().addLink( subTransField, DictionaryConst.LINK_DERIVES, streamFieldNode );
        }
      }
    } catch ( KettleStepException e ) {
      log.warn( Messages.getString( "WARN.SubtransAnalyzer.StepNotFound", resultStepName,
              subTransMeta.getName() ), e );
    }
  }

  /**
   * Create a metaverse node for the specified field in a subtrans.  This method assumes that something else has already
   * analyzed the subtrans once and the node already exists in the graph; we're just recreating it so we can link to it.
   * @param subTransMeta TransMeta of the subtrans to analyze
   * @param resultStepName name of the step to check for output fields
   * @param step StepMeta of the step to check for output fields
   * @param fieldValueMeta ValueMetaInterface of the field we want
   * @param stepFieldDescriptor IComponentDescriptor of the step in the subtrans graph
   * @return the IMetaverseNode representing the field, or null if the field isn't found in the graph
   */
  private IMetaverseNode getNodeForField( TransMeta subTransMeta, String resultStepName, StepMeta step,
                                          ValueMetaInterface fieldValueMeta,
                                          IComponentDescriptor stepFieldDescriptor ) {

    // see if the target step has a step after it in the subtrans
    IMetaverseNode subTransField = null;
    List<StepMeta> nextSteps = subTransMeta.findNextSteps( step );
    for ( StepMeta nextStep : nextSteps ) {

      subTransField = stepAnalyzer.createFieldNode( stepFieldDescriptor, fieldValueMeta,
        nextStep.getName(), false );
      // if there's more than one output step, the first "guess" at how the field was named could be wrong
      if ( null != ( (BaseMetaverseBuilder) stepAnalyzer.getMetaverseBuilder() ).getVertexForNode( subTransField ) ) {
        break;
      } else {
        subTransField = null;
      }
    }

    if ( null == subTransField ) {
      if ( !nextSteps.isEmpty() ) {
        log.warn( Messages.getString(
          "WARN.SubtransAnalyzer.FieldNotFound", resultStepName, subTransMeta.getName() ) );
      }
      subTransField = stepAnalyzer.createFieldNode( stepFieldDescriptor, fieldValueMeta, StepAnalyzer.NONE, false );
    }
    return subTransField;
  }
}

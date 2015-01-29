/*
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

package com.pentaho.metaverse.analyzer.kettle.step.mergejoin;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzer;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

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
public class MergeJoinStepAnalyzer extends BaseStepAnalyzer<MergeJoinMeta> {

  protected RowMetaInterface leftStepFields;
  protected RowMetaInterface rightStepFields;

  @Override
  public IMetaverseNode analyze( IComponentDescriptor descriptor, MergeJoinMeta mergeJoinMeta )
    throws MetaverseAnalyzerException {

    IMetaverseNode node = super.analyze( descriptor, mergeJoinMeta );

    // create links for the merging of the input streams on field(s)
    String[] keyFields1 = mergeJoinMeta.getKeyFields1();
    String[] keyFields2 = mergeJoinMeta.getKeyFields2();
    String joinType = mergeJoinMeta.getJoinType();

    node.setProperty( DictionaryConst.PROPERTY_JOIN_TYPE, joinType );
    node.setProperty( DictionaryConst.PROPERTY_JOIN_FIELDS_LEFT, Arrays.asList( keyFields1 ) );
    node.setProperty( DictionaryConst.PROPERTY_JOIN_FIELDS_RIGHT, Arrays.asList( keyFields2 ) );

    boolean isInner = MergeJoinMeta.join_types[0].equals( joinType );
    boolean isLeftOuter = MergeJoinMeta.join_types[1].equals( joinType );
    boolean isRightOuter = MergeJoinMeta.join_types[2].equals( joinType );
    boolean isFullOuter = MergeJoinMeta.join_types[3].equals( joinType );

    IMetaverseNode fieldNode1 = null;
    IMetaverseNode fieldNode2 = null;

    for ( int i = 0; i < keyFields1.length; i++ ) {
      fieldNode1 = createNodeFromDescriptor(
        getPrevStepFieldOriginDescriptor( descriptor, keyFields1[i], leftStepFields ) );

      fieldNode2 = createNodeFromDescriptor(
        getPrevStepFieldOriginDescriptor( descriptor, keyFields2[i], rightStepFields ) );

      // add the uses links between the input fields and the step
      if ( fieldNode1 != null ) {
        metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, fieldNode1 );
      }
      if ( fieldNode2 != null ) {
        metaverseBuilder.addLink( node, DictionaryConst.LINK_USES, fieldNode2 );
      }

      // handle links for join types between fields
      if ( ( isInner || isLeftOuter || isFullOuter ) && fieldNode1 != null && fieldNode2 != null ) {
        metaverseBuilder.addLink( fieldNode1, DictionaryConst.LINK_JOINS, fieldNode2 );
      }
      if ( ( isInner || isRightOuter || isFullOuter ) && fieldNode1 != null && fieldNode2 != null ) {
        metaverseBuilder.addLink( fieldNode2, DictionaryConst.LINK_JOINS, fieldNode1 );
      }
    }

    // if any fields where created by this step it was due to a name collision.
    // example: join fields in both input steps named COUNTRY. the second (right) field gets renamed with a suffix
    // on the way out of the step. You end up with COUNTRY (from the left) & COUNTRY_1 (from the right)
    if ( stepFields != null ) {
      List<ValueMetaInterface> valueMetas = stepFields.getValueMetaList();
      for ( ValueMetaInterface valueMeta : valueMetas ) {
        String fieldName = valueMeta.getName();
        if ( !fieldNameExistsInInput( fieldName ) ) {
          // chop off the _NN suffix
          String unsuffixName = fieldName.replaceAll( "_\\d*$", "" );
          IMetaverseNode originalField = createNodeFromDescriptor(
            getPrevStepFieldOriginDescriptor( descriptor, unsuffixName, rightStepFields ) );
          IMetaverseNode contributingField = createNodeFromDescriptor(
            getPrevStepFieldOriginDescriptor( descriptor, unsuffixName, leftStepFields ) );
          IMetaverseNode renamedField = createNodeFromDescriptor(
            getStepFieldOriginDescriptor( descriptor, fieldName ) );
          metaverseBuilder.addLink( originalField, DictionaryConst.LINK_DERIVES, renamedField );
          metaverseBuilder.addLink( contributingField, DictionaryConst.LINK_DERIVES, renamedField );
        }
      }
    }

    return node;
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
      rowMeta = new HashMap<String, RowMetaInterface>();
      try {
        StepMeta stepMeta1 = meta.getStepIOMeta().getInfoStreams().get( 0 ).getStepMeta();
        leftStepFields = parentTransMeta.getStepFields( stepMeta1 );

        StepMeta stepMeta2 = meta.getStepIOMeta().getInfoStreams().get( 1 ).getStepMeta();
        rightStepFields = parentTransMeta.getStepFields( stepMeta2 );
        rowMeta.put( stepMeta1.getName(), leftStepFields );
        rowMeta.put( stepMeta2.getName(), rightStepFields );

      } catch ( Throwable t ) {
        prevFields = null;
      }
    }
    return rowMeta;
  }

  @Override
  protected boolean fieldNameExistsInInput( String fieldName ) {
    boolean isInLeftInput = leftStepFields != null && leftStepFields.searchValueMeta( fieldName ) != null;
    boolean isInRightInput = rightStepFields != null && rightStepFields.searchValueMeta( fieldName ) != null;
    return isInLeftInput || isInRightInput;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> set = new HashSet<Class<? extends BaseStepMeta>>( 1 );
    set.add( MergeJoinMeta.class );
    return set;
  }

  // ******** Start - Used to aid in unit testing **********
  protected Map<String, RowMetaInterface> getPrevFields() {
    return prevFields;
  }

  protected RowMetaInterface getStepFields() {
    return stepFields;
  }

  protected void setBaseStepMeta( MergeJoinMeta meta ) {
    baseStepMeta = meta;
  }

  protected void setParentTransMeta( TransMeta parent ) {
    parentTransMeta = parent;
  }

  protected void setParentStepMeta( StepMeta parent ) {
    parentStepMeta = parent;
  }
  // ******** End - Used to aid in unit testing **********

}

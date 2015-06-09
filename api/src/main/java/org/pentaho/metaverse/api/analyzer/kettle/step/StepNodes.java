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
 *
 */

package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rfellows on 5/6/15.
 */
public class StepNodes {

  // use LinkedHashMap to preserve order in which elements are added
  private LinkedHashMap<String, LinkedHashMap<String, IMetaverseNode>> store;

  public StepNodes() {
    store = new LinkedHashMap<>();
  }

  public void addNode( String stepName, String fieldName, IMetaverseNode node ) {

    if ( !store.containsKey( stepName ) ) {
      store.put( stepName, new LinkedHashMap<String, IMetaverseNode>() );
    }

    Map<String, IMetaverseNode> stepFields = store.get( stepName );

    stepFields.put( fieldName, node );

  }

  public List<IMetaverseNode> findNodes( String fieldName ) {
    List<IMetaverseNode> nodes = new ArrayList<>();

    for ( Map<String, IMetaverseNode> stepNodes : store.values() ) {
      IMetaverseNode match = stepNodes.get( fieldName );
      if ( match != null ) {
        nodes.add( match );
      }
    }

    return nodes;
  }

  public IMetaverseNode findNode( StepField stepField ) {
    return findNode( stepField.getStepName(), stepField.getFieldName() );
  }

  public IMetaverseNode findNode( String stepName, String fieldName ) {
    IMetaverseNode node = null;

    Map<String, IMetaverseNode> stepNodes = store.get( stepName );
    if ( stepNodes != null ) {
      node = stepNodes.get( fieldName );
    }
    return node;
  }

  public Set<String> getStepNames() {
    return store.keySet();
  }

  public Set<String> getFieldNames( String stepName ) {
    Map<String, IMetaverseNode> stepNodes = store.get( stepName );
    Set<String> fieldNames = null;
    if ( stepNodes != null ) {
      fieldNames = stepNodes.keySet();
    }
    return fieldNames;
  }

  public Set<StepField> getFieldNames() {
    Set<StepField> fieldNames = new LinkedHashSet<>();
    for ( String stepName : getStepNames() ) {
      Set<String> names = getFieldNames( stepName );
      for ( String name : names ) {
        fieldNames.add( new StepField( stepName, name ) );
      }
    }
    return fieldNames;
  }

}

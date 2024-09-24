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

package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.pentaho.di.core.util.StringUtil;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;

import java.util.ArrayList;
import java.util.HashMap;
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
  private LowerCaseKeyLinkedHashMap<String, LowerCaseKeyLinkedHashMap<String, IMetaverseNode>> store;

  public StepNodes() {
    store = new LowerCaseKeyLinkedHashMap<>();
  }

  public void addNode( String stepName, String fieldName, IMetaverseNode node ) {

    if ( !store.containsKey( stepName ) ) {
      store.put( stepName, new LowerCaseKeyLinkedHashMap<String, IMetaverseNode>() );
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

  /**
   * An implementation of {@link LinkedHashMap} that provides case-insensitive lookup on keys. Ensures
   * that step and field name lookup is not case sensitive, in case some step pulls fields using the wrong case.
   */
  static class LowerCaseKeyLinkedHashMap<K, V> extends LinkedHashMap<String, V> {

    private Map<String, String> keyMap = new HashMap<>();

    @Override
    public V put( final String key, final V value ) {
      keyMap.put( StringUtil.safeToLowerCase( key ), key );
      return super.put( key, value );
    }

    @Override
    public V get( final Object key  ) {
      return super.get( keyMap.get( StringUtil.safeToLowerCase( key ) ) );
    }

    @Override
    public boolean containsKey( final Object key ) {
      return super.containsKey( keyMap.get( StringUtil.safeToLowerCase( key ) ) );
    }
  }
}

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

package org.pentaho.metaverse.api;


import java.util.List;

/**
 * Defines Analyzers responsible for handling external connections
 * @param <T> Object (connection) type to analyze
 * @param <S> Type of object to get the used connection objects from
 */
public interface IConnectionAnalyzer<T, S> extends IAnalyzer<IMetaverseNode, T> {

  public List<T> getUsedConnections( S meta );

  public IComponentDescriptor buildComponentDescriptor( IComponentDescriptor parentDescriptor, T connection );

}

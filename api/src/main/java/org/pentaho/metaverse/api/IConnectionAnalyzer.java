/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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

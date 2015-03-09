package com.pentaho.metaverse.analyzer.kettle;


import org.pentaho.platform.api.metaverse.IAnalyzer;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

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

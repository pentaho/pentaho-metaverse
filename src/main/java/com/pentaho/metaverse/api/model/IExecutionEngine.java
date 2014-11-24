package com.pentaho.metaverse.api.model;

/**
 * The IExecutionEngine interface describes a Pentaho execution engine.
 *
 * A Pentaho execution engine is any product that can operate on Pentaho documents/artifacts. For example,
 * Pentaho Data Integration is a Pentaho execution engine as it operates on Transformations and Jobs.
 */
public interface IExecutionEngine extends IVersionInfo {

  // This does nothing extra for now besides providing version, name, and description

}

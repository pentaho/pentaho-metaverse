/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.metaverse.api.model;

/**
 * The IExecutionEngine interface describes a Pentaho execution engine.
 *
 * A Pentaho execution engine is any product that can operate on Pentaho documents/artifacts. For example,
 * Pentaho Data Integration is a Pentaho execution engine as it operates on Transformations and Jobs.
 */
public interface IExecutionEngine extends IVersionInfo {

  // This does nothing extra for now besides providing version, name, and description

}

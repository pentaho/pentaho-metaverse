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

package org.pentaho.metaverse.api.model;

/**
 * The IExecutionEngine interface describes a Hitachi Vantara execution engine.
 *
 * A Hitachi Vantara execution engine is any product that can operate on Hitachi Vantara documents/artifacts. For example,
 * Pentaho Data Integration is a Hitachi Vantara execution engine as it operates on Transformations and Jobs.
 */
public interface IExecutionEngine extends IVersionInfo {

  // This does nothing extra for now besides providing version, name, and description

}

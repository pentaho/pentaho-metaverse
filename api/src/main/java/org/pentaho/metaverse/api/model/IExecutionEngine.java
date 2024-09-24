/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

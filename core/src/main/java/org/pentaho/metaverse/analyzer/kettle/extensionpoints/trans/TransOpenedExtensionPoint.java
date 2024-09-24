/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.pentaho.metaverse.messages.Messages;

/**
 * An extension point to create a lineage graph for an opened transformation
 */
@ExtensionPoint(
  description = "Transformation Lineage Graph creator",
  extensionPointId = "TransAfterOpen",
  id = "transOpenLineageGraph" )
public class TransOpenedExtensionPoint implements ExtensionPointInterface {

  /**
   * This method is called by the Kettle code
   *
   * @param log    the logging channel to log debugging information to
   * @param object The subject object that is passed to the plugin code
   * @throws org.pentaho.di.core.exception.KettleException If an error has occurred and the parent process should stop.
   */
  @Override
  public void callExtensionPoint( final LogChannelInterface log, Object object ) throws KettleException {
    if ( !MetaverseConfig.isLineageExecutionEnabled() ) {
      return;
    }
    if ( object instanceof TransMeta ) {
      try {
        TransMeta transMeta = (TransMeta) object;
        TransExtensionPointUtil.addLineageGraph( transMeta );
      } catch ( MetaverseException me ) {
        if ( log != null && log.isDebug() ) {
          log.logDebug( Messages.getString( "ERROR.Graph.CouldNotCreate", me.getMessage() ) );
        }
      }
    }
  }
}

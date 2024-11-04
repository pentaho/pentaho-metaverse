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


package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.listeners.ContentChangedListener;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.impl.MetaverseConfig;

/**
 * An extension point to maintain a lineage graph for an active transformation
 */
@ExtensionPoint(
  description = "Transformation Lineage Graph creator",
  extensionPointId = "TransChanged",
  id = "transChangeLineageGraph" )
public class TransChangedExtensionPoint implements ExtensionPointInterface, ContentChangedListener {

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
      TransMeta transMeta = (TransMeta) object;
      if ( !transMeta.getContentChangedListeners().contains( this ) ) {
        transMeta.addContentChangedListener( this );
      }
    }
  }

  /**
   * This method will be called when the parent object to which this listener is added, has been changed.
   *
   * @param object The changed object.
   */
  @Override
  public void contentChanged( Object object ) {
    updateLineage( object );
  }

  /**
   * This method will be called when the parent object has been declared safe (or saved, persisted, ...)
   *
   * @param object The safe object.
   */
  @Override
  public void contentSafe( Object object ) {
    updateLineage( object );
  }

  protected void updateLineage( Object object ) {
    if ( object instanceof TransMeta ) {
      try {
        TransMeta transMeta = (TransMeta) object;
        TransExtensionPointUtil.addLineageGraph( transMeta );
      } catch ( MetaverseException me ) {
        // Nothing we can do here
      }
    }
  }
}

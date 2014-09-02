/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.metaverse.graph.BlueprintsGraphMetaverseReader;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.io.FileUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.Trans;

import java.io.File;
import java.io.IOException;

/**
 * User: RFellows Date: 9/2/14
 */
@ExtensionPoint(
  description = "Transformation Finished metadata appender",
  extensionPointId = "TransformationFinish",
  id = "transFinishedRuntimeMetaverse" )
public class TransformationFinishedExtensionPoint implements ExtensionPointInterface {
  @Override public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    Trans trans = (Trans) object;

    Graph g = RuntimeGraphExtensionPointManager.getInstance().getRuntimeGraph( trans.getLogChannelId() );
    if ( g != null ) {
      // get the vertex that represents the runtime node
      Vertex vertex = g.getVertex( trans.getLogChannelId() );

      vertex.setProperty( "numberOfErrors", String.valueOf( trans.getResult().getNrErrors() ) );
      vertex.setProperty( "rowsWritten", String.valueOf( trans.getResult().getNrLinesWritten() ) );
      vertex.setProperty( "rowsRead", String.valueOf( trans.getResult().getNrLinesRead() ) );
      vertex.setProperty( "rowsInput", String.valueOf( trans.getResult().getNrLinesInput() ) );
      vertex.setProperty( "rowsOutput", String.valueOf( trans.getResult().getNrLinesOutput() ) );

      File exportFile = new File( trans.getTransMeta().getName() + " - export.graphml" );
      BlueprintsGraphMetaverseReader metaverseReader = new BlueprintsGraphMetaverseReader( g );
      try {
        FileUtils.writeStringToFile( exportFile, metaverseReader.exportToXml(), "UTF-8" );
      } catch ( IOException e ) {
        e.printStackTrace();
      } finally {
        RuntimeGraphExtensionPointManager.getInstance().removeRuntimeGraph( trans.getLogChannelId() );
      }

    } else {
      System.out.println( "No runtime graph found" );
    }

  }
}

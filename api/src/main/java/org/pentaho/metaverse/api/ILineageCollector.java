/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.api;

import java.io.OutputStream;
import java.util.List;

public interface ILineageCollector {

  /**
   * Returns a list of all lineage related artifacts (files) known to the system
   * @return
   */
  List<String> listArtifacts();

  /**
   * Returns a list of all lineage related artifacts (files) known to the system
   * starting at the given date (inclusive)
   * @param startingDate
   * @return
   * @throws IllegalArgumentException when the starting date is not a valid/parseable date
   */
  List<String> listArtifacts( String startingDate ) throws IllegalArgumentException;

  /**
   * Returns a list of all lineage related artifacts (files) known to the system
   * between two given dates (inclusive)
   * @param startingDate
   * @param endingDate
   * @return
   * @throws IllegalArgumentException when the a date is not a valid/parseable date
   */
  List<String> listArtifacts( final String startingDate, final String endingDate ) throws IllegalArgumentException;

  /**
   * Returns all of the lineage related artifacts for a given ktr/kjb
   * @param pathToArtifact Full path to an artifact that lineage is requested of
   * @return
   */
  List<String> listArtifactsForFile( String pathToArtifact );

  /**
   * Returns all of the lineage related artifacts for a given ktr/kjb starting at the given date (inclusive)
   * @param pathToArtifact
   * @param startingDate
   * @return
   * @throws IllegalArgumentException when the starting date is not a valid/parseable date
   */
  List<String> listArtifactsForFile( String pathToArtifact, String startingDate ) throws IllegalArgumentException;

  /**
   * Returns all of the lineage related artifacts for a given ktr/kjb between two given dates (inclusive)
   * @param pathToArtifact
   * @param startingDate
   * @param endingDate
   * @return
   * @throws IllegalArgumentException when the a date is not a valid/parseable date
   */
  List<String> listArtifactsForFile( String pathToArtifact, String startingDate, String endingDate )
    throws IllegalArgumentException;

  /**
   * Compresses files in zip format onto the provided OutputStream
   * @param paths List of file paths to add to the zip file
   * @param os OutputStream to write the zip file to
   */
  void compressArtifacts( List<String> paths, OutputStream os );

}

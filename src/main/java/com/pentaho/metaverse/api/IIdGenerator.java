/*!
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

package com.pentaho.metaverse.api;

/**
 * An IIdGenerator generates unique ids for a given type of artifact. Artifact types include
 * transformations, jobs, database tables and columns, file system text files etc. Every metaverse 
 * @author jdixon
 *
 */
public interface IIdGenerator {

  /**
   * Returns the types of artifacts that this id generator supports
   * @return An array of supported types
   */
  String[] getTypes();

  /**
   * Returns an id given a collection of tokens that represent an artifact. The number of tokens
   * is variable because some artifacts only need one e.g. a file system path where as others
   * such as a database column need more.
   * @param tokens A list of tokens that can be used to create an id
   * @return The unique id for the artifact
   */
  String getId( String... tokens );

}

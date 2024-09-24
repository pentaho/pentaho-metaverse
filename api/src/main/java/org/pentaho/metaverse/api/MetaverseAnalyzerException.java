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

package org.pentaho.metaverse.api;

/**
 * The MetaverseAnalyzerException class represents an unexpected error that occurs during analysis.
 * 
 */
public class MetaverseAnalyzerException extends MetaverseException {

  /**
   * Default ID for serialization
   */
  private static final long serialVersionUID = -4755192809625931317L;

  /**
   * Instantiates a new default metaverse analyzer exception.
   */
  public MetaverseAnalyzerException() {
    super();
  }

  /**
   * Instantiates a new metaverse analyzerexception with the specified message.
   * 
   * @param message
   *          the message
   */
  public MetaverseAnalyzerException( String message ) {
    super( message );
  }

  /**
   * Instantiates a new metaverse exception from an existing Throwable.
   * 
   * @param t
   *          the Throwable to wrap
   */
  public MetaverseAnalyzerException( Throwable t ) {
    super( t );
  }

  /**
   * Instantiates a new metaverse analyzerexception with the specified message and an underlying exception
   * @param message the message
   * @param t the Throwable to wrap
   */
  public MetaverseAnalyzerException( String message, Throwable t ) {
    super( message, t );
  }

}

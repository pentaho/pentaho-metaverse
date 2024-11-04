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
 * MetaverseException is the base class representing an unexpected error while interacting with the metaverse.
 * 
 */
public class MetaverseException extends Exception {

  /** Default ID for serialization. */
  private static final long serialVersionUID = -947058716721047769L;

  /**
   * Instantiates a new default metaverse exception.
   */
  public MetaverseException() {
    super();
  }

  /**
   * Instantiates a new metaverse exception with the specified message.
   * 
   * @param message
   *          the message
   */
  public MetaverseException( String message ) {
    super( message );
  }

  /**
   * Instantiates a new metaverse exception from an existing Throwable.
   * 
   * @param t
   *          the Throwable to wrap
   */
  public MetaverseException( Throwable t ) {
    super( t );

  }
  /**
   * Instantiates a new metaverse exception from an existing Throwable
   * with the specified new message.
   *
   * @param message
   *          the new exception message
   * @param t
   *          the Throwable to wrap
   */
  public MetaverseException( String message, Throwable t ) {
    super( message, t );
  }

}

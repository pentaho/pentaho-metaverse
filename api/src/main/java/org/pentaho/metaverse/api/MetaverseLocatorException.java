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

/**
 * The MetaverseLocatorException class represents an unexpected error that occurs during a
 * metaverse source repository scan.
 * 
 */
public class MetaverseLocatorException extends MetaverseException {

  /**
   * Default ID for serialization
   */
  private static final long serialVersionUID = 5725877205157962898L;

  /**
   * Instantiates a new default metaverse locator exception.
   */
  public MetaverseLocatorException() {
    super();
  }

  /**
   * Instantiates a new metaverse locator exception with the specified message.
   *
   * @param message
   *          the message
   */
  public MetaverseLocatorException( String message ) {
    super( message );
  }

  /**
   * Instantiates a new metaverse exception from an existing Throwable.
   *
   * @param t
   *          the Throwable to wrap
   */
  public MetaverseLocatorException( Throwable t ) {
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
  public MetaverseLocatorException( String message, Throwable t ) {
    super( message, t );
  }

}

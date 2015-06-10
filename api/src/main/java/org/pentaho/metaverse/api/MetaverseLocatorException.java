/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

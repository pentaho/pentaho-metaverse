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

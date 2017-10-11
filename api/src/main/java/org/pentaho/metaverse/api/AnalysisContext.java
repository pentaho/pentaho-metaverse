/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
 * AnalysisContext is a reference implementation of IAnalysisContext, providing mutators and accessors (i.e. getters
 * and setters) for the context name and associated object.
 */
public class AnalysisContext implements IAnalysisContext {

  protected String contextName;

  protected Object contextObject;

  /**
   * Protected default no-arg constructor, not to be used but protected for unit testing
   */
  protected AnalysisContext() {
    contextName = null;
    contextObject = null;
  }

  /**
   * Creates a new analysis context with the given name and associated object.
   *
   * @param contextName   the name of this context
   * @param contextObject the object associated with this context
   */
  public AnalysisContext( String contextName, Object contextObject ) {
    this.contextName = contextName;
    this.contextObject = contextObject;
  }

  /**
   * Creates a new analysis context with the given name and associated object.
   *
   * @param contextName the name of this context
   */
  public AnalysisContext( String contextName ) {
    this.contextName = contextName;
    this.contextObject = null;
  }

  /**
   * Gets the name of this context
   *
   * @return the String name of the context
   */
  @Override
  public String getContextName() {
    return contextName;
  }

  /**
   * Gets the object associated with this context
   *
   * @return the Object associated with the context (Trans for runtime analysis, e.g.)
   */
  @Override
  public Object getContextObject() {
    return contextObject;
  }

  /**
   * Sets the context name to the given value.
   *
   * @param contextName the context name to set
   */
  public void setContextName( String contextName ) {
    this.contextName = contextName;
  }

  /**
   * Associates the given object with this context.
   *
   * @param contextObject the object to associate with the context
   */
  public void setContextObject( Object contextObject ) {
    this.contextObject = contextObject;
  }
}

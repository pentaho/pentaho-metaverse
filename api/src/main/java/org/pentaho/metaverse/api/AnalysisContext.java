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

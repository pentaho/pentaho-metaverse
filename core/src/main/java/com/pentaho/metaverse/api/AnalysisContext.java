/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

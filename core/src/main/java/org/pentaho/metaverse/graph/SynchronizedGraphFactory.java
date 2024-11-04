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


package org.pentaho.metaverse.graph;

import org.pentaho.metaverse.api.model.BaseSynchronizedGraphFactory;

/**
 * <p>
 * Thin wrapper around {@link com.tinkerpop.blueprints.GraphFactory}
 * that constructs {@link SynchronizedGraph} objects.
 * </p>
 * <p>
 * <strong>NOTE:</strong> The backing graph configured <em>must</em> implement
 * {@link com.tinkerpop.blueprints.KeyIndexableGraph}
 * </p>
 */
public class SynchronizedGraphFactory extends BaseSynchronizedGraphFactory {

}

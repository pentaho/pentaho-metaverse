/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.metaverse.api.analyzer.kettle;

/**
 * MetaClassProvider is a helper interface that allows the users to retrieve a "metadata class" associated with the
 * implementation.
 */
public interface MetaClassProvider<T> {

  Class<T> getMetaClass();
}

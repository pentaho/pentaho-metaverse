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



package org.pentaho.metaverse.api;

import org.pentaho.metaverse.api.model.catalog.LineageDataResource;

import java.util.List;

public interface ICatalogLineageClient {

  boolean urlConfigured();

  void processLineage( List<LineageDataResource> inputSources, List<LineageDataResource> outputTargets );
}

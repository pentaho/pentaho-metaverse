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

import org.pentaho.metaverse.api.model.catalog.LineageDataResource;

import java.util.List;

public interface ICatalogLineageClient {

  boolean urlConfigured();

  void processLineage( List<LineageDataResource> inputSources, List<LineageDataResource> outputTargets );
}

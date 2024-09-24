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

public interface ICatalogLineageClientProvider {

  ICatalogLineageClient getCatalogLineageClient( String catalogUrl,
                                                 String catalogUsername,
                                                 String catalogPassword,
                                                 String catalogTokenUrl,
                                                 String catalogClientId,
                                                 String catalogClientSecret );
}

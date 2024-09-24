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

package org.pentaho.metaverse.analyzer.kettle.step.tableinput;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.steps.tableinput.TableInput;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TableInputExternalResourceConsumer extends BaseStepExternalResourceConsumer<TableInput, TableInputMeta> {

  private static TableInputExternalResourceConsumer instance;

  @VisibleForTesting
  protected TableInputExternalResourceConsumer() {
  }

  public static TableInputExternalResourceConsumer getInstance() {
    if ( null == instance ) {
      instance = new TableInputExternalResourceConsumer();
    }
    return instance;
  }

  @Override
  public Class<TableInputMeta> getMetaClass() {
    return TableInputMeta.class;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( TableInputMeta meta, IAnalysisContext context ) {

    Set<IExternalResourceInfo> resources = new HashSet<IExternalResourceInfo>();
    DatabaseMeta dbMeta = meta.getDatabaseMeta();
    if ( dbMeta != null ) {
      IExternalResourceInfo databaseResource = ExternalResourceInfoFactory.createDatabaseResource( dbMeta, true );

      String query = context.equals( DictionaryConst.CONTEXT_RUNTIME )
        ? meta.getParentStepMeta().getParentTransMeta().environmentSubstitute( meta.getSQL() )
        : meta.getSQL();

      databaseResource.getAttributes().put( DictionaryConst.PROPERTY_QUERY, query );

      resources.add( databaseResource );
    }
    return resources;
  }
}

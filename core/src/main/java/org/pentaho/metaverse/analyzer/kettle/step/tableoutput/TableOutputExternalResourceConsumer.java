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


package org.pentaho.metaverse.analyzer.kettle.step.tableoutput;

import com.cronutils.utils.VisibleForTesting;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutput;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class TableOutputExternalResourceConsumer
  extends BaseStepExternalResourceConsumer<TableOutput, TableOutputMeta> {

  private static TableOutputExternalResourceConsumer instance;

  @VisibleForTesting
  protected TableOutputExternalResourceConsumer() {
  }

  public static TableOutputExternalResourceConsumer getInstance() {
    if ( null == instance ) {
      instance = new TableOutputExternalResourceConsumer();
    }
    return instance;
  }

  @Override
  public Class<TableOutputMeta> getMetaClass() {
    return TableOutputMeta.class;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( TableOutputMeta meta, IAnalysisContext context ) {
    Set<IExternalResourceInfo> resources = new HashSet<IExternalResourceInfo>();
    DatabaseMeta dbMeta = meta.getDatabaseMeta();
    if ( dbMeta != null ) {
      IExternalResourceInfo databaseResource = ExternalResourceInfoFactory.createDatabaseResource( dbMeta, false );

      String tableName = context.equals( DictionaryConst.CONTEXT_RUNTIME )
        ? meta.getParentStepMeta().getParentTransMeta().environmentSubstitute( meta.getTableName() )
        : meta.getTableName();

      String schema = context.getContextName().equals( DictionaryConst.CONTEXT_RUNTIME )
        ? meta.getParentStepMeta().getParentTransMeta().environmentSubstitute( meta.getSchemaName() )
        : meta.getSchemaName();

      databaseResource.getAttributes().put( DictionaryConst.PROPERTY_TABLE, tableName );
      databaseResource.getAttributes().put( DictionaryConst.PROPERTY_SCHEMA, schema );
      resources.add( databaseResource );
    }
    return resources;
  }
}

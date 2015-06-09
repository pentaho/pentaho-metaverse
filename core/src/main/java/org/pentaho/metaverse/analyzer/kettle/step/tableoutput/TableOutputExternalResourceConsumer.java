/*
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

package org.pentaho.metaverse.analyzer.kettle.step.tableoutput;

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

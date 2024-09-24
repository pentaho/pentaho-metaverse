/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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

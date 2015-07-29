/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import org.pentaho.di.trans.Trans;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobLineageHolderMap;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.util.MetaverseBeanUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a singleton that provides a map from Jobs to LineageHolder objects, and can/should be used
 * by runtime elements (such as extension points) to access shared lineage artifacts (MetaverseBuilder and
 * Execution Profile, e.g.)
 */
public class TransLineageHolderMap {

  private static TransLineageHolderMap INSTANCE = new TransLineageHolderMap();

  private Map<Trans, LineageHolder> lineageHolderMap = new ConcurrentHashMap<>();

  private IMetaverseBuilder defaultMetaverseBuilder;

  private TransLineageHolderMap() {
    // Private constructor to enforce Singleton pattern
  }

  public static TransLineageHolderMap getInstance() {
    return INSTANCE;
  }

  /**
   * For testing, injection, etc.
   *
   * @param instance
   */
  protected static void setInstance( final TransLineageHolderMap instance ) {
    INSTANCE = instance;
  }

  public LineageHolder getLineageHolder( Trans t ) {
    LineageHolder holder = lineageHolderMap.get( t );
    if ( holder == null ) {
      holder = new LineageHolder();
      putLineageHolder( t, holder );
    }
    return holder;
  }

  public void putLineageHolder( Trans t, LineageHolder holder ) {
    lineageHolderMap.put( t, holder );
  }

  public IMetaverseBuilder getMetaverseBuilder( Trans trans ) {
    if ( trans != null ) {
      if ( trans.getParentJob() == null && trans.getParentTrans() == null ) {
        IMetaverseBuilder builder =
          this.getLineageHolder( trans ).getMetaverseBuilder();
        if ( builder == null ) {
          return getDefaultMetaverseBuilder();
        } else {
          return builder;
        }
      } else {
        if ( trans.getParentJob() != null ) {
          // Get the builder for the job
          return JobLineageHolderMap.getInstance().getMetaverseBuilder( trans.getParentJob() );
        } else {
          return this.getMetaverseBuilder( trans.getParentTrans() );
        }
      }
    }
    return null;
  }

  protected IMetaverseBuilder getDefaultMetaverseBuilder() {
    // always try to get a new builder if this method is called. otherwise we will end up with overlapping graphs
    IMetaverseBuilder newBuilder = (IMetaverseBuilder) MetaverseBeanUtil.getInstance().get( "IMetaverseBuilderPrototype" );
    if ( newBuilder == null ) {
      return defaultMetaverseBuilder;
    } else {
      return newBuilder;
    }
  }

  protected void setDefaultMetaverseBuilder( IMetaverseBuilder builder ) {
    defaultMetaverseBuilder = builder;
  }
}

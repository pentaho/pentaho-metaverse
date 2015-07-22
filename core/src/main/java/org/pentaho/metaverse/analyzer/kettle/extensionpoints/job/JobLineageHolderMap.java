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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.job;

import org.pentaho.di.job.Job;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransLineageHolderMap;
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
public class JobLineageHolderMap {

  private static JobLineageHolderMap INSTANCE = new JobLineageHolderMap();

  private IMetaverseBuilder defaultMetaverseBuilder;

  private Map<Job, LineageHolder> lineageHolderMap = new ConcurrentHashMap<>();

  private JobLineageHolderMap() {
    // Private constructor to enforce Singleton pattern
  }

  public static JobLineageHolderMap getInstance() {
    return INSTANCE;
  }

  /**
   * For testing, injection, etc.
   *
   * @param instance
   */
  protected static void setInstance( final JobLineageHolderMap instance ) {
    INSTANCE = instance;
  }

  public LineageHolder getLineageHolder( Job job ) {
    LineageHolder holder = lineageHolderMap.get( job );
    if ( holder == null ) {
      holder = new LineageHolder();
      lineageHolderMap.put( job, holder );
    }
    return holder;
  }

  public void putLineageHolder( Job job, LineageHolder holder ) {
    lineageHolderMap.put( job, holder );
  }

  public IMetaverseBuilder getMetaverseBuilder( Job job ) {
    if ( job != null ) {
      if ( job.getParentJob() == null && job.getParentTrans() == null ) {
        IMetaverseBuilder builder =
          this.getLineageHolder( job ).getMetaverseBuilder();
        if ( builder == null ) {
          return getDefaultMetaverseBuilder();
        } else {
          return builder;
        }
      } else {
        if ( job.getParentJob() != null ) {
          // Get the builder for the job
          return this.getMetaverseBuilder( job.getParentJob() );
        } else {
          return TransLineageHolderMap.getInstance().getMetaverseBuilder( job.getParentTrans() );
        }
      }
    }
    return null;
  }

  protected IMetaverseBuilder getDefaultMetaverseBuilder() {
    if ( defaultMetaverseBuilder == null ) {
      defaultMetaverseBuilder = (IMetaverseBuilder) MetaverseBeanUtil.getInstance().get( "IMetaverseBuilderPrototype" );
    }
    return defaultMetaverseBuilder;
  }

  protected void setDefaultMetaverseBuilder( IMetaverseBuilder builder ) {
    defaultMetaverseBuilder = builder;
  }

}

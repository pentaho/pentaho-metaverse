/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.extensionpoints.job;

import com.google.common.collect.MapMaker;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransLineageHolderMap;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.impl.MetaverseBuilder;

import java.util.Collections;
import java.util.Map;

/**
 * This class is a singleton that provides a map from Jobs to LineageHolder objects, and can/should be used
 * by runtime elements (such as extension points) to access shared lineage artifacts (MetaverseBuilder and
 * Execution Profile, e.g.)
 */
public class JobLineageHolderMap {

  private static JobLineageHolderMap INSTANCE = new JobLineageHolderMap();

  private IMetaverseBuilder defaultMetaverseBuilder;

  private Map<Job, LineageHolder> lineageHolderMap =
    Collections.synchronizedMap( new MapMaker().weakKeys().makeMap() );

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

  /**
   * To be called ONLY assuming that {@link Job} {@code job} has no parents, or because its parent is being removed.
   * @param job an instance of {@link Job} being removed from {@code lineageHolderMap}
   * @return the {@link LineageHolder} beign removed from {@code lineageHolderMap}
   */
  public LineageHolder removeLineageHolderImpl( final Job job  ) {
    final LineageHolder holder = lineageHolderMap.remove( job );

    for ( final Object subExecutable : holder.getSubTransAndJobs() ) {
      if ( subExecutable instanceof Trans ) {
        TransLineageHolderMap.getInstance().removeLineageHolderImpl( (Trans) subExecutable );
      } else if ( subExecutable instanceof  Job ) {
        removeLineageHolderImpl( (Job) subExecutable );
      }
    }
    return holder;
  }

  public LineageHolder removeLineageHolder( Job job  ) {
    // remove the job only if it has no parent - if it does have a parent, the holder might be needed at a later time
    // and will be removed when the parent is removed
    if ( job.getParentTrans() == null && job.getParentJob() == null ) {
      return removeLineageHolderImpl( job );
    } else if ( job.getParentTrans() != null ) {
      TransLineageHolderMap.getInstance().getLineageHolder( job.getParentTrans() ).addSubTransOrJob( job );
    } else if ( job.getParentJob() != null ) {
      getLineageHolder( job.getParentJob() ).addSubTransOrJob( job );
    }
    return null;
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
    // always try to get a new builder if this method is called. otherwise we will end up with overlapping graphs
    IMetaverseBuilder newBuilder = new MetaverseBuilder();
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

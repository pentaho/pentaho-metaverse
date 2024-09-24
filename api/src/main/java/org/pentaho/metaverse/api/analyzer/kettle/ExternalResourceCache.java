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

package org.pentaho.metaverse.api.analyzer.kettle;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metaverse.api.IMetaverseConfig;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A cache of resources encountered when a transformation is run that can be used to access these run-time resources at
 * a later time, when data lineage analysis occurs.
 */
public class ExternalResourceCache {

  private static final Logger log = LoggerFactory.getLogger( ExternalResourceCache.class );

  protected static final long DEFAULT_TIMEOUT_SECONDS = 6L * 60 * 60; // 6 hours

  protected Cache<String, ExternalResourceValues> resourceCache;

  private static ExternalResourceCache INSTANCE;

  public static ExternalResourceCache getInstance() {
    if ( INSTANCE == null ) {
      synchronized ( ExternalResourceCache.class ) {
        if ( INSTANCE == null ) {
          INSTANCE = new ExternalResourceCache();
        }
      }
    }
    return INSTANCE;
  }

  private ExternalResourceCache() {
    this( PentahoSystem.get( IMetaverseConfig.class ) );
  }

  protected ExternalResourceCache( IMetaverseConfig config ) {
    long cacheExpireTime = getCacheExpireTime( config );
    initCache( cacheExpireTime, TimeUnit.SECONDS );
  }

  protected long getCacheExpireTime( IMetaverseConfig config ) {
    String expireTime = ( config != null ) ? config.getExternalResourceCacheExpireTime() : null;
    return ( expireTime != null ) ? Long.parseLong( expireTime ) : DEFAULT_TIMEOUT_SECONDS;
  }

  void initCache( long time, TimeUnit timeUnit ) {
    resourceCache = CacheBuilder.newBuilder().expireAfterAccess( time, timeUnit ).build();
    log.debug( "{} cache expire time set to {} {}", this.getClass().getSimpleName(), time, timeUnit );
  }

  protected String getUniqueId( final StepMeta meta ) {

    if ( meta == null || meta.getParentTransMeta() == null ) {
      return null;
    }
    final TransMeta transMeta = meta.getParentTransMeta();
    if ( transMeta.getRepository() == null ) {
      String transName = transMeta.getFilename() == null ? transMeta.getName() : transMeta.getFilename();
      return KettleAnalyzerUtil.normalizeFilePathSafely( transName ) + "::" + meta.getName();
    } else {
      return transMeta.getPathAndName() + "." + transMeta.getDefaultExtension()
        + "::" + meta.getName();
    }
  }

  public void removeCachedResources( final Trans trans ) {
    final TransMeta transMeta = trans.getTransMeta();
    final List<StepMeta> steps = transMeta.getSteps();
    for ( final StepMeta step : steps ) {
      final String uniqueMetaId = getUniqueId( step );
      resourceCache.invalidate( uniqueMetaId );
    }
  }

  public Resources<IExternalResourceInfo> get( final BaseStepMeta meta ) {
    if ( meta == null ) {
      return null;
    }
    final String uniqueMetaId = getUniqueId( meta.getParentStepMeta() );
    synchronized ( this ) {
      return resourceCache.getIfPresent( uniqueMetaId );
    }
  }

  public void cache( final BaseStepMeta meta, final ExternalResourceValues resources ) {
    final String uniqueMetaId = getUniqueId( meta.getParentStepMeta() );
    synchronized ( this ) {
      resourceCache.put( uniqueMetaId, resources );
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    if ( resourceCache != null ) {
      for ( Map.Entry<String, ExternalResourceValues> entry : resourceCache.asMap().entrySet() ) {
        sb.append( " ---- " + entry.getKey() + ": " + entry.getValue().size() + "\n" );
        sb.append( entry.getValue().toString() );
      }
    }

    log.debug( "\n {}", sb );
    return sb.toString();
  }

  public ExternalResourceValues newExternalResourceValues() {
    return new ExternalResourceValues();
  }

  public class ExternalResourceValues extends Resources<IExternalResourceInfo> {

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      for ( IExternalResourceInfo resource : internal ) {
        sb.append( " ............ " ).append( resource.getClass().getSimpleName() ).append( " " ).append( resource
          .getName() ).append( " (" ).append( resource.hashCode() ).append( ") \n" );
      }
      return sb.toString();
    }

  }

  /**
   * A wrapper class that provides controlled access to a {@link Set} of values.
   *
   * @param <V> the type of object stored in the internal {@link Set}.
   */
  public abstract class Resources<V> {

    protected Set<V> internal = new ConcurrentHashSet();

    public void add( final V value ) {
      internal.add( value );
    }

    public boolean remove( final V value ) {
      return internal.remove( value );
    }

    public int size() {
      return internal.size();
    }

    public boolean contains( final V value ) {
      return internal.contains( value );
    }

    /**
     * Returns a copy of the original {@code internal} {@link Set}
     *
     * @return a copy of the original {@code internal} {@link Set}
     */
    public Set<V> getInternal() {
      return new HashSet( this.internal );
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      for ( V value : internal ) {
        sb.append( " ............ " ).append( value.getClass().getSimpleName() ).append( " " ).append(
          value.hashCode() ).append( "\n" );
      }
      return sb.toString();
    }
  }
}

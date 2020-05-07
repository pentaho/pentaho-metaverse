/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.analyzer.kettle;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache of resources encountered when a transformation is run that can be used to access these run-time resources at
 * a later time, when data lineage analysis occurs.
 */
public class ExternalResourceCache {

  private static final Logger log = LoggerFactory.getLogger( ExternalResourceCache.class );

  protected volatile Map<String, TransValues> transMap = new ConcurrentHashMap();

  protected volatile Map<String, ExternalResourceValues> resourceMap = new ConcurrentHashMap();

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

      Resources<Trans> transformations = transMap.get( uniqueMetaId );
      if ( transformations != null && transformations.contains( trans ) ) {
        transformations.remove( trans );
      }
      // are there any other potentially running transformations that might be using this resource? If not, the
      // resource can be removed, otherwise keep it
      if ( transformations == null || transformations.size() == 0 ) {
        resourceMap.remove( uniqueMetaId );
        transMap.remove( uniqueMetaId );
      }
    }
  }

  public Resources<IExternalResourceInfo> get( final Trans trans, final BaseStepMeta meta ) {
    if ( meta == null ) {
      return null;
    }
    cacheTrans( trans, meta );
    final String uniqueMetaId = getUniqueId( meta.getParentStepMeta() );
    return resourceMap.get( uniqueMetaId );
  }

  private void cacheTrans( final Trans trans, final BaseStepMeta meta ) {
    if ( trans != null ) {
      final String uniqueMetaId = getUniqueId( meta.getParentStepMeta() );
      TransValues transformations = transMap.get( uniqueMetaId );
      if ( transformations == null ) {
        transformations = new TransValues();
        transMap.put( uniqueMetaId, transformations );
      }
      transformations.add( trans );
    }
  }

  public void cache( final Trans trans, final BaseStepMeta meta, final ExternalResourceValues resources ) {
    cacheTrans( trans, meta );
    final String uniqueMetaId = getUniqueId( meta.getParentStepMeta() );
    resourceMap.put( uniqueMetaId, resources );
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    for ( Map.Entry<String, ExternalResourceValues> entry : resourceMap.entrySet() ) {
      sb.append( " ---- " + entry.getKey() + ": " + entry.getValue().size() + "\n" );
      sb.append( entry.getValue().toString() );
    }
    for ( Map.Entry<String, TransValues> entry : transMap.entrySet() ) {
      sb.append( " ---- " + entry.getKey() + ": " + entry.getValue().size() + "\n" );
      sb.append( entry.getValue().toString() );
    }

    log.debug( "\n" + sb.toString() );
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

  public class TransValues extends Resources<Trans> {

    @Override
    public void add ( final Trans value ) {
      List<Trans> internalList = new ArrayList<>();
      internalList.addAll( internal );
      for ( Trans t : internalList ) {
        if ( t.getTransMeta().equals( value.getTransMeta() ) ) {
          return;
        }
      }
      super.add( value );
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

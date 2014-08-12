/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.analyzer.kettle;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.step.GenericStepMetaAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.step.IStepAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.step.IStepAnalyzerProvider;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.platform.api.metaverse.IAnalyzer;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The TransformationAnalyzer class is responsible for gathering transformation metadata, creating links
 * to form relationships between the transformation and its child collaborators (ie, step, dbMetas), and
 * calling the analyzers responsible for providing the metadata for the child collaborators.
 */
public class TransformationAnalyzer extends BaseDocumentAnalyzer {

  private static final long serialVersionUID = 3147152759123052372L;

  private static final Set<String> defaultSupportedTypes = new HashSet<String>() {
    /**
     * Default serial ID for serialization
     */
    private static final long serialVersionUID = -7433589337075366681L;

    // Statically add the supported types to the set
    {
      add( "ktr" );
    }
  };

  private IStepAnalyzerProvider stepAnalyzerProvider;

  private Logger log = LoggerFactory.getLogger( TransformationAnalyzer.class );

  @Override
  public IMetaverseNode analyze( IMetaverseComponentDescriptor descriptor, IMetaverseDocument document )
    throws MetaverseAnalyzerException {

    validateState( document );

    Object repoObject = document.getContent();

    TransMeta transMeta = null;
    if ( repoObject instanceof String ) {
      // hydrate the transformation
      try {
        String content = (String) repoObject;
        ByteArrayInputStream xmlStream = new ByteArrayInputStream( content.getBytes() );
        transMeta = new TransMeta( xmlStream, null, false, null, null );
      } catch ( KettleXMLException e ) {
        throw new MetaverseAnalyzerException( e );
      } catch ( KettleMissingPluginsException e ) {
        throw new MetaverseAnalyzerException( e );
      }
    } else if ( repoObject instanceof TransMeta ) {
      transMeta = (TransMeta) repoObject;
    }

    // Create a metaverse node and start filling in details
    IMetaverseNode node = metaverseObjectFactory.createNodeObject(
        descriptor.getStringID(),
        transMeta.getName(),
        DictionaryConst.NODE_TYPE_TRANS );

    // pull out the standard fields
    String description = transMeta.getDescription();
    node.setProperty( "description", description );

    Date createdDate = transMeta.getCreatedDate();
    node.setProperty( "createdDate", createdDate );

    Date lastModifiedDate = transMeta.getModifiedDate();
    node.setProperty( "lastModifiedDate", lastModifiedDate );

    // handle the step
    for ( int stepNr = 0; stepNr < transMeta.nrSteps(); stepNr++ ) {
      StepMeta stepMeta = transMeta.getStep( stepNr );
      try {
        if ( stepMeta != null ) {
          if ( stepMeta.getParentTransMeta() == null ) {
            stepMeta.setParentTransMeta( transMeta );
          }

          IMetaverseNode stepNode = null;
          IMetaverseComponentDescriptor stepDescriptor =
              getChildComponentDescriptor( descriptor, stepMeta.getName(), DictionaryConst.NODE_TYPE_TRANS_STEP );
          Set<IStepAnalyzer> stepAnalyzers = getStepAnalyzers( stepMeta );
          if ( stepAnalyzers != null && !stepAnalyzers.isEmpty() ) {
            for ( IStepAnalyzer stepAnalyzer : stepAnalyzers ) {
              stepAnalyzer.setMetaverseBuilder( metaverseBuilder );
              stepNode = stepAnalyzer.analyze( stepDescriptor, getBaseStepMetaFromStepMeta( stepMeta ) );
            }
          } else {
            IAnalyzer<BaseStepMeta> defaultStepAnalyzer = new GenericStepMetaAnalyzer();
            defaultStepAnalyzer.setMetaverseBuilder( metaverseBuilder );
            stepNode = defaultStepAnalyzer.analyze( stepDescriptor, getBaseStepMetaFromStepMeta( stepMeta ) );
          }
          if ( stepNode != null ) {
            metaverseBuilder.addLink( node, DictionaryConst.LINK_CONTAINS, stepNode );
          }
        }
      } catch ( MetaverseAnalyzerException mae ) {
        //Don't throw an exception, just log and carry on
        log.error( "Error processing " + stepMeta.getName(), mae );
      }
    }

    metaverseBuilder.addNode( node );
    addParentLink( descriptor, node );
    return node;
  }

  /**
   * Returns a set of strings corresponding to which types of content are supported by this analyzer
   *
   * @return the supported types (as a set of Strings)
   * @see org.pentaho.platform.api.metaverse.IDocumentAnalyzer#getSupportedTypes()
   */
  public Set<String> getSupportedTypes() {
    return defaultSupportedTypes;
  }

  protected Set<IStepAnalyzer> getStepAnalyzers( final StepMeta stepMeta ) {

    Set<IStepAnalyzer> stepAnalyzers = new HashSet<IStepAnalyzer>();

    // Attempt to discover a BaseStepMeta from the given StepMeta
    BaseStepMeta baseStepMeta = getBaseStepMetaFromStepMeta( stepMeta );
    stepAnalyzerProvider = getStepAnalyzerProvider();
    if ( stepAnalyzerProvider != null ) {
      if ( baseStepMeta == null ) {
        stepAnalyzers.addAll( stepAnalyzerProvider.getAnalyzers() );
      } else {
        Set<Class<?>> analyzerClassSet = new HashSet<Class<?>>( 1 );
        analyzerClassSet.add( baseStepMeta.getClass() );
        stepAnalyzers.addAll( stepAnalyzerProvider.getAnalyzers( analyzerClassSet ) );
      }
    } else {
      stepAnalyzers.add( new GenericStepMetaAnalyzer() );
    }

    return stepAnalyzers;
  }

  protected void setStepAnalyzerProvider( IStepAnalyzerProvider stepAnalyzerProvider ) {
    this.stepAnalyzerProvider = stepAnalyzerProvider;
  }

  /**
   * Retrieves the step analyzer provider. This is used to find step-specific analyzers
   *
   * @return the IKettleStepAnalyzer provider instance that provides step-specific analyzers
   */
  protected IStepAnalyzerProvider getStepAnalyzerProvider() {
    if ( stepAnalyzerProvider != null ) {
      return stepAnalyzerProvider;
    }
    stepAnalyzerProvider = (IStepAnalyzerProvider) PentahoSystem.get( IStepAnalyzerProvider.class );
    return stepAnalyzerProvider;
  }

  protected BaseStepMeta getBaseStepMetaFromStepMeta( StepMeta stepMeta ) {

    // Attempt to discover a BaseStepMeta from the given StepMeta
    BaseStepMeta baseStepMeta = new BaseStepMeta();
    baseStepMeta.setParentStepMeta( stepMeta );
    if ( stepMeta != null ) {
      StepMetaInterface smi = stepMeta.getStepMetaInterface();
      if ( smi instanceof BaseStepMeta ) {
        baseStepMeta = (BaseStepMeta) smi;
      }
    }
    return baseStepMeta;
  }

}

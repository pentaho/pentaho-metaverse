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

package org.pentaho.metaverse.analyzer.kettle;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.step.GenericStepMetaAnalyzer;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IMetaverseLink;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.api.PropertiesHolder;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzerProvider;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * The TransformationAnalyzer class is responsible for gathering transformation metadata, creating links
 * to form relationships between the transformation and its child collaborators (ie, step, dbMetas), and
 * calling the analyzers responsible for providing the metadata for the child collaborators.
 */
public class TransformationAnalyzer extends BaseDocumentAnalyzer {

  private static final long serialVersionUID = 3147152759123052372L;

  protected static final Set<String> defaultSupportedTypes = new HashSet<String>() {
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

  private static final Logger log = LoggerFactory.getLogger( TransformationAnalyzer.class );

  @Override
  public synchronized IMetaverseNode analyze( IComponentDescriptor descriptor, IDocument document )
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
        transMeta.setFilename( document.getStringID() );
        if ( transMeta.hasMissingPlugins() ) {
          throw new MetaverseAnalyzerException( Messages.getErrorString( "ERROR.MissingPlugin" ) );
        }
      } catch ( KettleException e ) {
        throw new MetaverseAnalyzerException( e );
      }
    } else if ( repoObject instanceof TransMeta ) {
      transMeta = (TransMeta) repoObject;
    }

    Trans t = new Trans( transMeta );
    t.setInternalKettleVariables( transMeta );

    IComponentDescriptor documentDescriptor = new MetaverseComponentDescriptor( document.getStringID(),
      DictionaryConst.NODE_TYPE_TRANS, new Namespace( descriptor.getLogicalId() ), descriptor.getContext() );

    // Create a metaverse node and start filling in details
    IMetaverseNode node = metaverseObjectFactory.createNodeObject(
      document.getNamespace(),
      transMeta.getName(),
      DictionaryConst.NODE_TYPE_TRANS );
    node.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

    // pull out the standard fields
    String description = transMeta.getDescription();
    if ( description != null ) {
      node.setProperty( DictionaryConst.PROPERTY_DESCRIPTION, description );
    }

    String extendedDescription = transMeta.getExtendedDescription();
    if ( extendedDescription != null ) {
      node.setProperty( "extendedDescription", extendedDescription );
    }

    Date createdDate = transMeta.getCreatedDate();
    if ( createdDate != null ) {
      node.setProperty( DictionaryConst.PROPERTY_CREATED, Long.toString( createdDate.getTime() ) );
    }

    String createdUser = transMeta.getCreatedUser();
    if ( createdUser != null ) {
      node.setProperty( DictionaryConst.PROPERTY_CREATED_BY, createdUser );
    }

    Date lastModifiedDate = transMeta.getModifiedDate();
    if ( lastModifiedDate != null ) {
      node.setProperty( DictionaryConst.PROPERTY_LAST_MODIFIED, Long.toString( lastModifiedDate.getTime() ) );
    }

    String lastModifiedUser = transMeta.getModifiedUser();
    if ( lastModifiedUser != null ) {
      node.setProperty( DictionaryConst.PROPERTY_LAST_MODIFIED_BY, lastModifiedUser );
    }

    String version = transMeta.getTransversion();
    if ( version != null ) {
      node.setProperty( DictionaryConst.PROPERTY_ARTIFACT_VERSION, version );
    }

    String status = Messages.getString( "INFO.JobOrTrans.Status_" + Integer.toString( transMeta.getTransstatus() ) );
    if ( status != null && !status.startsWith( "!" ) ) {
      node.setProperty( DictionaryConst.PROPERTY_STATUS, status );
    }

    node.setProperty( DictionaryConst.PROPERTY_PATH, document.getProperty( DictionaryConst.PROPERTY_PATH ) );

    String[] parameters = transMeta.listParameters();
    if ( parameters != null ) {
      for ( String parameter : parameters ) {
        try {
          // Determine parameter properties and add them to a map, then the map to the list
          String defaultParameterValue = transMeta.getParameterDefault( parameter );
          String parameterValue = transMeta.getParameterValue( parameter );
          String parameterDescription = transMeta.getParameterDescription( parameter );
          PropertiesHolder paramProperties = new PropertiesHolder();
          paramProperties.setProperty( "defaultValue", defaultParameterValue );
          paramProperties.setProperty( "value", parameterValue );
          paramProperties.setProperty( "description", parameterDescription );
          node.setProperty( "parameter_" + parameter, paramProperties.toString() );
        } catch ( UnknownParamException upe ) {
          // This shouldn't happen as we're using the list provided by the meta
          throw new MetaverseAnalyzerException( upe );
        }
      }
    }
    // handle the step
    for ( int stepNr = 0; stepNr < transMeta.nrSteps(); stepNr++ ) {
      StepMeta stepMeta = transMeta.getStep( stepNr );
      try {
        if ( stepMeta != null ) {
          if ( stepMeta.getParentTransMeta() == null ) {
            stepMeta.setParentTransMeta( transMeta );
          }

          IMetaverseNode stepNode = null;
          IComponentDescriptor stepDescriptor = new MetaverseComponentDescriptor( stepMeta.getName(),
            DictionaryConst.NODE_TYPE_TRANS_STEP, node, documentDescriptor.getContext() );
          Set<IStepAnalyzer> stepAnalyzers = getStepAnalyzers( stepMeta );
          if ( stepAnalyzers != null && !stepAnalyzers.isEmpty() ) {
            for ( IStepAnalyzer stepAnalyzer : stepAnalyzers ) {
              stepAnalyzer.setMetaverseBuilder( metaverseBuilder );
              stepNode = (IMetaverseNode) stepAnalyzer.analyze( stepDescriptor, getBaseStepMetaFromStepMeta( stepMeta ) );
            }
          } else {
            GenericStepMetaAnalyzer defaultStepAnalyzer = new GenericStepMetaAnalyzer();
            defaultStepAnalyzer.setMetaverseBuilder( metaverseBuilder );
            stepNode = defaultStepAnalyzer.analyze( stepDescriptor, getBaseStepMetaFromStepMeta( stepMeta ) );
          }
          if ( stepNode != null ) {
            metaverseBuilder.addLink( node, DictionaryConst.LINK_CONTAINS, stepNode );
          }
        }
      } catch ( Throwable mae ) {
        //Don't throw an exception, just log and carry on
        log.warn( Messages.getString( "ERROR.ErrorDuringAnalysis", stepMeta.getName(),
          Const.NVL( mae.getLocalizedMessage(), "Unspecified" ) ) );
        log.debug( Messages.getString( "ERROR.ErrorDuringAnalysisStackTrace" ), mae );
      }
    }

    // Model the hops between steps
    int numHops = transMeta.nrTransHops();
    for ( int i = 0; i < numHops; i++ ) {
      TransHopMeta hop = transMeta.getTransHop( i );
      StepMeta fromStep = hop.getFromStep();
      StepMeta toStep = hop.getToStep();
      INamespace childNs = new Namespace( node.getLogicalId() );

      // process legitimate hops
      if ( fromStep != null && toStep != null ) {
        IMetaverseNode fromStepNode = metaverseObjectFactory.createNodeObject(
          childNs,
          fromStep.getName(),
          DictionaryConst.NODE_TYPE_TRANS_STEP );

        IMetaverseNode toStepNode = metaverseObjectFactory.createNodeObject(
          childNs,
          toStep.getName(),
          DictionaryConst.NODE_TYPE_TRANS_STEP );

        // Create and decorate the link between the steps
        IMetaverseLink link = metaverseObjectFactory.createLinkObject();
        link.setFromNode( fromStepNode );
        link.setLabel( DictionaryConst.LINK_HOPSTO );
        link.setToNode( toStepNode );

        // Is this hop enabled?
        link.setProperty( DictionaryConst.PROPERTY_ENABLED, hop.isEnabled() );

        // Add metadata about the type of stream (target, error, info) it is. Default to "target".
        String linkType = "target";
        if ( fromStep.isSendingErrorRowsToStep( toStep ) ) {
          linkType = "error";
        } else {
          String[] infoStepnames = toStep.getStepMetaInterface().getStepIOMeta().getInfoStepnames();
          // If the "from" step is the source of an info stream to the "to" step, it's an "info" hop
          if ( Const.indexOfString( fromStep.getName(), infoStepnames ) >= 0 ) {
            linkType = "info";
          }
        }
        link.setProperty( DictionaryConst.PROPERTY_TYPE, linkType );
        metaverseBuilder.addLink( link );
      }
    }

    metaverseBuilder.addNode( node );
    addParentLink( documentDescriptor, node );
    return node;
  }

  /**
   * Returns a set of strings corresponding to which types of content are supported by this analyzer
   *
   * @return the supported types (as a set of Strings)
   * @see IDocumentAnalyzer#getSupportedTypes()
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

  public void setStepAnalyzerProvider( IStepAnalyzerProvider stepAnalyzerProvider ) {
    this.stepAnalyzerProvider = stepAnalyzerProvider;
  }

  /**
   * Retrieves the step analyzer provider. This is used to find step-specific analyzers
   *
   * @return the IKettleStepAnalyzer provider instance that provides step-specific analyzers
   */
  public IStepAnalyzerProvider getStepAnalyzerProvider() {
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

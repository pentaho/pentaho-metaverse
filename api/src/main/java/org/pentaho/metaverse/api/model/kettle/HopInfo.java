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


package org.pentaho.metaverse.api.model.kettle;

import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.trans.TransHopMeta;

/**
 * User: RFellows Date: 11/3/14
 */
public class HopInfo implements IHopInfo {
  private String fromStepName;
  private String toStepName;
  private String type;
  private boolean enabled = true;

  public HopInfo() {
  }

  public HopInfo( TransHopMeta transHopMeta ) {
    if ( transHopMeta.getFromStep() != null && transHopMeta.getToStep() != null ) {
      setFromStepName( transHopMeta.getFromStep().getName() );
      setToStepName( transHopMeta.getToStep().getName() );
    }
    setEnabled( transHopMeta.isEnabled() );
  }

  public HopInfo( JobHopMeta jobHopMeta ) {
    if ( jobHopMeta.getFromEntry() != null && jobHopMeta.getToEntry() != null ) {
      setFromStepName( jobHopMeta.getFromEntry().getName() );
      setToStepName( jobHopMeta.getToEntry().getName() );
    }
    setEnabled( jobHopMeta.isEnabled() );
  }

  @Override public String getFromStepName() {
    return fromStepName;
  }

  @Override public String getToStepName() {
    return toStepName;
  }

  @Override public String getType() {
    return type;
  }

  @Override public boolean isEnabled() {
    return enabled;
  }

  public void setFromStepName( String fromStepName ) {
    this.fromStepName = fromStepName;
  }

  public void setToStepName( String toStepName ) {
    this.toStepName = toStepName;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public void setEnabled( boolean enabled ) {
    this.enabled = enabled;
  }
}

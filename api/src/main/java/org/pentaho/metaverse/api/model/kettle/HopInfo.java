/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

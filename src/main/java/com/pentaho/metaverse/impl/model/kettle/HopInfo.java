/*
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

package com.pentaho.metaverse.impl.model.kettle;

import com.pentaho.metaverse.api.model.kettle.IHopInfo;
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

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


package org.pentaho.metaverse.api.model;

import org.pentaho.metaverse.api.ChangeType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Created by mburgess on 2/4/15.
 */
public class Operations extends EnumMap<ChangeType, List<IOperation>> {

  public Operations() {
    super( ChangeType.class );
  }

  public void addOperation( ChangeType operationType, IOperation operation ) {
    List<IOperation> operations = get( operationType );
    if ( operations == null ) {
      operations = new ArrayList<IOperation>();
      put( operationType, operations );
    }
    operations.add( operation );

  }
}

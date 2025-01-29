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


package org.pentaho.metaverse.frames;

import com.tinkerpop.frames.Property;
import org.pentaho.dictionary.DictionaryConst;

public interface FilterRowsStepNode extends TransformationStepNode {

  @Property( DictionaryConst.PROPERTY_OPERATIONS )
  public String getOperations();

}

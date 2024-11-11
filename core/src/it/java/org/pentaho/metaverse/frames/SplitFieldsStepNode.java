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


package org.pentaho.metaverse.frames;

import com.tinkerpop.frames.Property;
import org.pentaho.dictionary.DictionaryConst;

/**
 * Created by rfellows on 3/26/15.
 */
public interface SplitFieldsStepNode extends TransformationStepNode {

  @Property( DictionaryConst.PROPERTY_DELIMITER )
  public String getDelimiter();

  @Property( DictionaryConst.PROPERTY_ENCLOSURE )
  public String getEnclosure();

}

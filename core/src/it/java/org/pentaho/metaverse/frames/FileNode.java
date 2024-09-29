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

public interface FileNode extends Concept {
  @Property( DictionaryConst.PROPERTY_PATH )
  String getPath();
}

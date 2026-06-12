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

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.pentaho.dictionary.DictionaryConst;

public class FilterRowsStepNode extends TransformationStepNode {
  public FilterRowsStepNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public String getOperations() {
    return getStringValue( DictionaryConst.PROPERTY_OPERATIONS );
  }
}

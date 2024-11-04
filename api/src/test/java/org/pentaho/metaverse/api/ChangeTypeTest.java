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


package org.pentaho.metaverse.api;

import org.junit.Test;
import org.pentaho.dictionary.DictionaryConst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ChangeTypeTest {

  @Test
  public void testToString() throws Exception {
    assertEquals( DictionaryConst.PROPERTY_METADATA_OPERATIONS, ChangeType.METADATA.toString() );
    assertEquals( DictionaryConst.PROPERTY_DATA_OPERATIONS, ChangeType.DATA.toString() );
  }

  @Test
  public void testForValue() {
    assertEquals( ChangeType.METADATA, ChangeType.forValue( DictionaryConst.PROPERTY_METADATA_OPERATIONS ) );
    assertEquals( ChangeType.DATA, ChangeType.forValue( DictionaryConst.PROPERTY_DATA_OPERATIONS ) );
    assertNull( ChangeType.forValue( "Not a real value" ) );
  }
}

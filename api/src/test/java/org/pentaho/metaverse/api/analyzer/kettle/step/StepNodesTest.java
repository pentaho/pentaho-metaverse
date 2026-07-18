/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StepNodesTest {

  @Test
  public void testLowerCaseKeyLinkedHashMap() {
    final Map<String, String> map = new StepNodes.LowerCaseKeyLinkedHashMap();
    map.put( "FOO", "FOO" );

    assertEquals( map.get( "FOO" ), "FOO" );
    assertEquals( map.get( "foo" ), "FOO" );

    assertTrue( map.containsKey( "FOO" ) );
    assertTrue( map.containsKey( "foo" ) );
  }
}

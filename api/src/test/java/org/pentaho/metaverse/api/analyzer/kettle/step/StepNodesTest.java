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


package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class StepNodesTest {

  @Test
  public void testLowerCaseKeyLinkedHashMap() {
    final Map<String, String> map = new StepNodes.LowerCaseKeyLinkedHashMap();
    map.put( "FOO", "FOO" );

    Assert.assertEquals( map.get( "FOO" ), "FOO" );
    Assert.assertEquals( map.get( "foo" ), "FOO" );

    Assert.assertTrue( map.containsKey( "FOO" ) );
    Assert.assertTrue( map.containsKey( "foo" ) );
  }
}

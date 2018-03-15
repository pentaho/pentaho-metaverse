/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

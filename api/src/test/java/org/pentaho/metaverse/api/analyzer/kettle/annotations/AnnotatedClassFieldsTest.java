/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.analyzer.kettle.annotations;

import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.pentaho.metaverse.api.analyzer.kettle.annotations.AnnotationDrivenStepMetaAnalyzerTest.getTestStepMeta;


public class AnnotatedClassFieldsTest {


  private AnnotatedClassFields fields = new AnnotatedClassFields( getTestStepMeta( new RowMeta() ) );


  @Test public void checkCounts() {
    assertThat( fields.props().count(), equalTo( 11L ) );
    assertThat( fields.nodes().count(), equalTo( 3L ) );
    assertThat( fields.links().count(), equalTo( 1L ) );
  }

  @Test public void testGetNode() {
    assertThat( fields.node( "test_name" ).isPresent(), equalTo( true ) );
    assertThat( fields.node( "test_name" ).get().annotation.type(), equalTo( "test_type" ) );
    assertThat( fields.node( "test_name" ).get().val(), equalTo( "ServernameOrWhatever" ) );
  }
}

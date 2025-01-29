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


package org.pentaho.metaverse.api.analyzer.kettle.annotations;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaPluginType;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.pentaho.metaverse.api.analyzer.kettle.annotations.AnnotationDrivenStepMetaAnalyzerTest.getTestStepMeta;


public class AnnotatedClassFieldsTest {


  private AnnotatedClassFields fields = new AnnotatedClassFields( getTestStepMeta( new RowMeta() ) );

  @BeforeClass
  public static void setupClass() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init();
  }

  @Test public void checkCounts() {
    assertThat( fields.props().count(), equalTo( 12L ) );
    assertThat( fields.nodes().count(), equalTo( 5L ) );
    assertThat( fields.links().count(), equalTo( 1L ) );
  }

  @Test public void testGetNode() {
    assertThat( fields.node( "test_name" ).isPresent(), equalTo( true ) );
    assertThat( fields.node( "test_name" ).get().annotation.type(), equalTo( "test_type" ) );
    assertThat( fields.node( "test_name" ).get().val(), equalTo( "ServernameOrWhatever" ) );
  }
}

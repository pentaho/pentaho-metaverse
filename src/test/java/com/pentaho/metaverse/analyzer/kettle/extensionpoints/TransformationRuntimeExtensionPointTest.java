/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.metaverse.impl.MetaverseNamespace;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )

public class TransformationRuntimeExtensionPointTest {

  private TransformationRuntimeExtensionPoint extensionPoint;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  private LogChannelInterface logChannel;

  @Mock
  private Trans mockTrans;

  @Mock
  private TransMeta mockTransMeta;

  @Mock
  MetaverseNamespace namespace;

  @Before
  public void setUp() throws Exception {
    extensionPoint = new TransformationRuntimeExtensionPoint();
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( factory );
    when( mockTrans.getTransMeta() ).thenReturn( mockTransMeta );
    when( namespace.getChildNamespace( anyString(), anyString() ) ).thenReturn( namespace );
    when( namespace.getParentNamespace() ).thenReturn( namespace );
    when( namespace.getNamespaceId() ).thenReturn( "namespace" );
  }

  @Test
  public void testCallExtensionPoint() throws Exception {
    when( mockTrans.getFilename() ).thenReturn( "src/it/resources/repo/demo/file_to_table.ktr" );
    extensionPoint.callExtensionPoint( logChannel, mockTrans );
  }
}

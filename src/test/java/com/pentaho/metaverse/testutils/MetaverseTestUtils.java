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

package com.pentaho.metaverse.testutils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;

import com.pentaho.dictionary.MetaverseLink;
import com.pentaho.dictionary.MetaverseTransientNode;
import com.pentaho.metaverse.impl.MetaverseDocument;

/**
 * @author mburgess
 * 
 */
public class MetaverseTestUtils {

  public static IMetaverseObjectFactory getMetaverseObjectFactory() {
    IMetaverseObjectFactory factory = mock( IMetaverseObjectFactory.class );

    when( factory.createNodeObject( Mockito.anyString() ) ).thenAnswer( new Answer<IMetaverseNode>() {

      @Override
      public IMetaverseNode answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        return new MetaverseTransientNode( (String) args[0] );
      }

    } );

    when( factory.createLinkObject() ).thenReturn( new MetaverseLink() );

    when( factory.createDocumentObject() ).thenReturn( new MetaverseDocument() );

    return factory;
  }

}
